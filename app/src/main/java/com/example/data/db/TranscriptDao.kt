package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TranscriptItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts ORDER BY updatedAt DESC")
    fun getAllTranscripts(): Flow<List<TranscriptItem>>

    @Query("SELECT * FROM transcripts WHERE id = :id")
    fun getTranscriptById(id: Long): Flow<TranscriptItem?>

    @Query("SELECT * FROM transcripts WHERE title LIKE '%' || :query || '%' OR rawText LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchTranscripts(query: String): Flow<List<TranscriptItem>>

    @Query("SELECT * FROM transcripts WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteTranscripts(): Flow<List<TranscriptItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(item: TranscriptItem): Long

    @Update
    suspend fun updateTranscript(item: TranscriptItem)

    @Delete
    suspend fun deleteTranscript(item: TranscriptItem)

    @Query("DELETE FROM transcripts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
