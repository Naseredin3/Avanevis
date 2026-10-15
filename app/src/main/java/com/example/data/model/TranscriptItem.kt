package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcripts")
data class TranscriptItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceFileName: String,
    val rawText: String,
    val timestampsText: String? = null,
    val summary: String? = null,
    val keyPoints: String? = null,
    val language: String = "فارسی",
    val durationSeconds: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    val wordCount: Int
        get() = if (rawText.isBlank()) 0 else rawText.trim().split(Regex("\\s+")).size

    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "--:--"
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
