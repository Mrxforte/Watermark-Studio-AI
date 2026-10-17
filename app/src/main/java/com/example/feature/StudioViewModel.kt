package com.example.feature

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.assistant.ChatMessage
import com.example.ai.assistant.GeminiAssistantService
import com.example.core.cv.InpaintingEngine
import com.example.core.cv.MethodSelector
import com.example.core.cv.ProcessingQualityAnalyzer
import com.example.core.cv.QualityReport
import com.example.core.media.SampleVideoGenerator
import com.example.core.media.VideoExportPipeline
import com.example.core.media.VideoFrameExtractor
import com.example.core.media.VideoMetadataHelper
import com.example.data.local.ExportEntity
import com.example.data.local.StudioDatabase
import com.example.data.repository.ExportRepository
import com.example.data.repository.ProjectRepository
import com.example.domain.model.ExportConfig
import com.example.domain.model.ExportResolution
import com.example.domain.model.MethodRecommendation
import com.example.domain.model.NormalizedRect
import com.example.domain.model.ProjectItem
import com.example.domain.model.RemovalMethod
import com.example.domain.model.VideoInfo
import com.example.domain.model.WatermarkDesign
import com.example.domain.model.WatermarkMask
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class ExportState {
    object Idle : ExportState()
    data class InProgress(val progress: Float, val statusMessage: String) : ExportState()
    data class Success(val file: File, val frames: Int, val bytes: Long) : ExportState()
    data class Failed(val error: String) : ExportState()
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudioDatabase.getInstance(application)
    private val projectRepo = ProjectRepository(db.studioDao())
    private val exportRepo = ExportRepository(db.studioDao())

    val savedProjects: StateFlow<List<ProjectItem>> = projectRepo.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedExports: StateFlow<List<ExportEntity>> = exportRepo.getExports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentVideo = MutableStateFlow<VideoInfo?>(null)
    val currentVideo: StateFlow<VideoInfo?> = _currentVideo.asStateFlow()

    private val _currentFrameBitmap = MutableStateFlow<Bitmap?>(null)
    val currentFrameBitmap: StateFlow<Bitmap?> = _currentFrameBitmap.asStateFlow()

    private val _processedPreviewBitmap = MutableStateFlow<Bitmap?>(null)
    val processedPreviewBitmap: StateFlow<Bitmap?> = _processedPreviewBitmap.asStateFlow()

    private val _currentMask = MutableStateFlow(WatermarkMask())
    val currentMask: StateFlow<WatermarkMask> = _currentMask.asStateFlow()

    private val _selectedMethod = MutableStateFlow(RemovalMethod.TELEA)
    val selectedMethod: StateFlow<RemovalMethod> = _selectedMethod.asStateFlow()

    private val _methodRecommendation = MutableStateFlow<MethodRecommendation?>(null)
    val methodRecommendation: StateFlow<MethodRecommendation?> = _methodRecommendation.asStateFlow()

    private val _qualityReport = MutableStateFlow<QualityReport?>(null)
    val qualityReport: StateFlow<QualityReport?> = _qualityReport.asStateFlow()

    private val _watermarkDesign = MutableStateFlow(WatermarkDesign())
    val watermarkDesign: StateFlow<WatermarkDesign> = _watermarkDesign.asStateFlow()

    private val _timelineThumbnails = MutableStateFlow<List<Bitmap>>(emptyList())
    val timelineThumbnails: StateFlow<List<Bitmap>> = _timelineThumbnails.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _compareSplitRatio = MutableStateFlow(0.5f)
    val compareSplitRatio: StateFlow<Float> = _compareSplitRatio.asStateFlow()

    private val _isSplitCompareEnabled = MutableStateFlow(false)
    val isSplitCompareEnabled: StateFlow<Boolean> = _isSplitCompareEnabled.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Welcome to Watermark Studio AI! I am your AI Computer Vision Advisor. I can analyze video frames, locate watermarks, recommend optimal inpainting strategies (Telea, Navier-Stokes, or Temporal), and guide you on creating professional video watermarks."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private var exportJob: Job? = null
    private var isExportCancelledFlag = false

    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            val info = VideoMetadataHelper.extractMetadata(getApplication(), uri)
            _currentVideo.value = info
            _currentTimeMs.value = 0L

            // Extract initial frame
            val frame = VideoFrameExtractor.extractFrame(getApplication(), uri, 0L)
            _currentFrameBitmap.value = frame

            // Extract timeline thumbnails
            val thumbs = VideoFrameExtractor.extractTimelineThumbnails(
                getApplication(), uri, info.durationMs, thumbnailCount = 10
            )
            _timelineThumbnails.value = thumbs

            // Run automatic method recommendation
            val recommendation = MethodSelector.selectOptimalMethod(frame, _currentMask.value.rect)
            _methodRecommendation.value = recommendation
            _selectedMethod.value = recommendation.recommendedMethod

            triggerPreviewReconstruction()
        }
    }

    fun loadSampleVideo() {
        viewModelScope.launch {
            val sampleUri = SampleVideoGenerator.getOrCreateSampleVideo(getApplication())
            importVideo(sampleUri)
        }
    }

    fun updateMaskRect(newRect: NormalizedRect) {
        _currentMask.value = _currentMask.value.copy(rect = newRect)
        // Refresh recommendation based on new area
        val frame = _currentFrameBitmap.value
        val recommendation = MethodSelector.selectOptimalMethod(frame, newRect)
        _methodRecommendation.value = recommendation
        triggerPreviewReconstruction()
    }

    fun updateMaskSettings(dilation: Int, feather: Int, inpaintRadius: Int) {
        _currentMask.value = _currentMask.value.copy(
            dilationRadius = dilation,
            featherRadius = feather,
            inpaintRadius = inpaintRadius
        )
        triggerPreviewReconstruction()
    }

    fun selectMethod(method: RemovalMethod) {
        _selectedMethod.value = method
        triggerPreviewReconstruction()
    }

    fun setCompareSplitRatio(ratio: Float) {
        _compareSplitRatio.value = ratio.coerceIn(0f, 1f)
    }

    fun toggleSplitCompare() {
        _isSplitCompareEnabled.value = !_isSplitCompareEnabled.value
    }

    fun seekToTime(timeMs: Long) {
        val video = _currentVideo.value ?: return
        val clampedTime = timeMs.coerceIn(0L, video.durationMs)
        _currentTimeMs.value = clampedTime

        viewModelScope.launch {
            val uri = Uri.parse(video.uriString)
            val frame = VideoFrameExtractor.extractFrame(getApplication(), uri, clampedTime)
            if (frame != null) {
                _currentFrameBitmap.value = frame
                triggerPreviewReconstruction()
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun updateWatermarkDesign(design: WatermarkDesign) {
        _watermarkDesign.value = design
    }

    private fun triggerPreviewReconstruction() {
        val source = _currentFrameBitmap.value ?: return
        viewModelScope.launch {
            val method = _selectedMethod.value
            val mask = _currentMask.value
            val tMs = _currentTimeMs.value

            val processed = InpaintingEngine.processFrame(
                sourceBitmap = source,
                mask = mask,
                method = method,
                timestampMs = tMs
            )
            _processedPreviewBitmap.value = processed

            // Quality analysis
            val report = ProcessingQualityAnalyzer.analyzeFrame(source, processed, mask.rect)
            _qualityReport.value = report
        }
    }

    fun startExport(
        resolution: ExportResolution = ExportResolution.ORIGINAL,
        fps: Int = 30,
        bitrateMbps: Float = 6.0f,
        isWatermarkCreation: Boolean = false
    ) {
        val video = _currentVideo.value ?: return
        isExportCancelledFlag = false
        _exportState.value = ExportState.InProgress(0.01f, "Initializing export pipeline...")

        val config = ExportConfig(
            resolution = resolution,
            fps = fps,
            bitrateMbps = bitrateMbps,
            removalMethod = _selectedMethod.value,
            mask = _currentMask.value,
            watermarkDesign = _watermarkDesign.value,
            isWatermarkCreationMode = isWatermarkCreation
        )

        exportJob = viewModelScope.launch {
            val result = VideoExportPipeline.exportVideo(
                context = getApplication(),
                videoInfo = video,
                config = config,
                onProgress = { p, msg ->
                    _exportState.value = ExportState.InProgress(p, msg)
                },
                isCancelled = { isExportCancelledFlag }
            )

            if (result.success && result.outputFile != null) {
                _exportState.value = ExportState.Success(
                    file = result.outputFile,
                    frames = result.totalFramesProcessed,
                    bytes = result.outputSizeBytes
                )
                // Record in database
                exportRepo.recordExport(
                    fileName = result.outputFile.name,
                    filePath = result.outputFile.absolutePath,
                    fileSizeBytes = result.outputSizeBytes,
                    durationMs = video.durationMs,
                    resolution = resolution.label,
                    methodOrDesign = if (isWatermarkCreation) "Creation: ${_watermarkDesign.value.name}" else "Removal: ${_selectedMethod.value.title}"
                )
            } else {
                _exportState.value = ExportState.Failed(result.errorMessage ?: "Export failed")
            }
        }
    }

    fun cancelExport() {
        isExportCancelledFlag = true
        exportJob?.cancel()
        _exportState.value = ExportState.Idle
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiThinking.value = true
            val result = GeminiAssistantService.sendChatMessage(_chatMessages.value, text)
            _isAiThinking.value = false

            val reply = result.getOrElse { "Could not process request." }
            val modelMsg = ChatMessage(isUser = false, text = reply)
            _chatMessages.value = _chatMessages.value + modelMsg
        }
    }

    fun analyzeCurrentFrameWithGemini() {
        val frame = _currentFrameBitmap.value ?: return
        val userMsg = ChatMessage(isUser = true, text = "Analyzing current video frame with Gemini 4-Argon (High Thinking)...", hasImage = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiThinking.value = true
            val result = GeminiAssistantService.analyzeFrameWithThinking(frame)
            _isAiThinking.value = false

            val reply = result.getOrElse { "Frame analysis failed." }
            val modelMsg = ChatMessage(isUser = false, text = reply)
            _chatMessages.value = _chatMessages.value + modelMsg
        }
    }

    fun saveCurrentProject(projectName: String) {
        val video = _currentVideo.value ?: return
        val name = if (projectName.isNotBlank()) projectName else "Project ${System.currentTimeMillis() % 10000}"
        viewModelScope.launch {
            projectRepo.saveProject(
                ProjectItem(
                    name = name,
                    videoUri = video.uriString,
                    durationMs = video.durationMs,
                    width = video.width,
                    height = video.height,
                    removalMethod = _selectedMethod.value,
                    mask = _currentMask.value,
                    watermarkText = _watermarkDesign.value.textConfig.text
                )
            )
        }
    }

    fun loadProject(item: ProjectItem) {
        viewModelScope.launch {
            val uri = Uri.parse(item.videoUri)
            importVideo(uri)
            _selectedMethod.value = item.removalMethod
            _currentMask.value = item.mask
            _watermarkDesign.value = _watermarkDesign.value.copy(
                textConfig = _watermarkDesign.value.textConfig.copy(text = item.watermarkText)
            )
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectRepo.deleteProject(id)
        }
    }

    fun deleteExport(id: String) {
        viewModelScope.launch {
            exportRepo.deleteExport(id)
        }
    }
}
