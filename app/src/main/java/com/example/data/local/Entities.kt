package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val videoUri: String,
    val thumbnailUri: String?,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val isWatermarkCreation: Boolean,
    val removalMethod: String,
    val maskLeft: Float,
    val maskTop: Float,
    val maskRight: Float,
    val maskBottom: Float,
    val dilationRadius: Int,
    val featherRadius: Int,
    val inpaintRadius: Int,
    val watermarkText: String,
    val watermarkOpacity: Float,
    val watermarkFontSize: Float,
    val repeatGridEnabled: Boolean,
    val updatedAt: Long
)

@Entity(tableName = "exports")
data class ExportEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val resolution: String,
    val methodOrDesign: String,
    val createdAt: Long
)
