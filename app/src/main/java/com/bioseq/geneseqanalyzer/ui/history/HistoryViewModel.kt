package com.bioseq.geneseqanalyzer.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bioseq.geneseqanalyzer.data.local.AppDatabase
import com.bioseq.geneseqanalyzer.data.local.SequenceEntity
import com.bioseq.geneseqanalyzer.repository.SavedSequenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SavedSequenceRepository
    private val searchQuery = MutableStateFlow("")

    val savedSequences = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allSavedSequences
        } else {
            repository.searchSequences(query)
        }
    }.asLiveData()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SavedSequenceRepository(database.sequenceDao())
    }

    fun search(query: String) {
        searchQuery.value = query
    }

    fun deleteSequence(sequence: SequenceEntity) {
        viewModelScope.launch {
            repository.deleteSequence(sequence)
        }
    }
}