package com.example.data.repository

import com.example.data.local.ExportEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.StudioDao
import com.example.domain.model.NormalizedRect
import com.example.domain.model.ProjectItem
import com.example.domain.model.RemovalMethod
import com.example.domain.model.WatermarkMask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val dao: StudioDao) {

    fun getProjects(): Flow<List<ProjectItem>> = dao.getAllProjects().map { list ->
        list.map { entity ->
            ProjectItem(
                id = entity.id,
                name = entity.name,
                videoUri = entity.videoUri,
                thumbnailUri = entity.thumbnailUri,
                durationMs = entity.durationMs,
                width = entity.width,
                height = entity.height,
                isWatermarkCreation = entity.isWatermarkCreation,
                removalMethod = try { RemovalMethod.valueOf(entity.removalMethod) } catch (_: Exception) { RemovalMethod.TELEA },
                mask = WatermarkMask(
                    rect = NormalizedRect(entity.maskLeft, entity.maskTop, entity.maskRight, entity.maskBottom),
                    dilationRadius = entity.dilationRadius,
                    featherRadius = entity.featherRadius,
                    inpaintRadius = entity.inpaintRadius
                ),
                watermarkText = entity.watermarkText,
                updatedAt = entity.updatedAt
            )
        }
    }

    suspend fun saveProject(project: ProjectItem) {
        val entity = ProjectEntity(
            id = project.id,
            name = project.name,
            videoUri = project.videoUri,
            thumbnailUri = project.thumbnailUri,
            durationMs = project.durationMs,
            width = project.width,
            height = project.height,
            isWatermarkCreation = project.isWatermarkCreation,
            removalMethod = project.removalMethod.name,
            maskLeft = project.mask.rect.left,
            maskTop = project.mask.rect.top,
            maskRight = project.mask.rect.right,
            maskBottom = project.mask.rect.bottom,
            dilationRadius = project.mask.dilationRadius,
            featherRadius = project.mask.featherRadius,
            inpaintRadius = project.mask.inpaintRadius,
            watermarkText = project.watermarkText,
            watermarkOpacity = 0.85f,
            watermarkFontSize = 24f,
            repeatGridEnabled = false,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertProject(entity)
    }

    suspend fun deleteProject(projectId: String) {
        val entity = dao.getProjectById(projectId)
        if (entity != null) dao.deleteProject(entity)
    }
}

class ExportRepository(private val dao: StudioDao) {
    fun getExports(): Flow<List<ExportEntity>> = dao.getAllExports()

    suspend fun recordExport(
        fileName: String,
        filePath: String,
        fileSizeBytes: Long,
        durationMs: Long,
        resolution: String,
        methodOrDesign: String
    ) {
        dao.insertExport(
            ExportEntity(
                id = java.util.UUID.randomUUID().toString(),
                fileName = fileName,
                filePath = filePath,
                fileSizeBytes = fileSizeBytes,
                durationMs = durationMs,
                resolution = resolution,
                methodOrDesign = methodOrDesign,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteExport(id: String) {
        dao.deleteExport(id)
    }
}
