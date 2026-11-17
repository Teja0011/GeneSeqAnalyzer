package com.bioseq.geneseqanalyzer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SequenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(sequence: SequenceEntity)

    @Query("SELECT * FROM saved_sequences ORDER BY timestamp DESC")
    fun getAllSequences(): Flow<List<SequenceEntity>>

    @Query("SELECT * FROM saved_sequences WHERE accessionId LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchSequences(query: String): Flow<List<SequenceEntity>>

    @Query("SELECT * FROM saved_sequences WHERE accessionId = :accessionId LIMIT 1")
    suspend fun getSequenceById(accessionId: String): SequenceEntity?

    @Delete
    suspend fun deleteSequence(sequence: SequenceEntity)

    @Query("DELETE FROM saved_sequences WHERE accessionId = :accessionId")
    suspend fun deleteSequenceById(accessionId: String)
}