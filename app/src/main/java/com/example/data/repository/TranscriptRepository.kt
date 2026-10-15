package com.example.data.repository

import com.example.data.db.TranscriptDao
import com.example.data.model.TranscriptItem
import kotlinx.coroutines.flow.Flow

class TranscriptRepository(private val dao: TranscriptDao) {
    val allTranscripts: Flow<List<TranscriptItem>> = dao.getAllTranscripts()
    val favoriteTranscripts: Flow<List<TranscriptItem>> = dao.getFavoriteTranscripts()

    fun getTranscriptById(id: Long): Flow<TranscriptItem?> = dao.getTranscriptById(id)

    fun search(query: String): Flow<List<TranscriptItem>> = dao.searchTranscripts(query)

    suspend fun saveTranscript(item: TranscriptItem): Long = dao.insertTranscript(item)

    suspend fun updateTranscript(item: TranscriptItem) = dao.updateTranscript(item)

    suspend fun deleteTranscript(item: TranscriptItem) = dao.deleteTranscript(item)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
