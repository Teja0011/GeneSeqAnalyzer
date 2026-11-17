package com.bioseq.geneseqanalyzer.repository

import com.bioseq.geneseqanalyzer.data.local.SequenceDao
import com.bioseq.geneseqanalyzer.data.local.SequenceEntity
import kotlinx.coroutines.flow.Flow

class SavedSequenceRepository(private val sequenceDao: SequenceDao) {

    val allSavedSequences: Flow<List<SequenceEntity>> = sequenceDao.getAllSequences()

    fun searchSequences(query: String): Flow<List<SequenceEntity>> {
        return sequenceDao.searchSequences(query.trim())
    }

    suspend fun saveSequence(sequence: SequenceEntity) {
        sequenceDao.insertSequence(sequence)
    }

    suspend fun deleteSequence(sequence: SequenceEntity) {
        sequenceDao.deleteSequence(sequence)
    }

    suspend fun deleteSequenceById(accessionId: String) {
        sequenceDao.deleteSequenceById(accessionId)
    }

    suspend fun isSequenceSaved(accessionId: String): Boolean {
        return sequenceDao.getSequenceById(accessionId) != null
    }
}