package com.bioseq.geneseqanalyzer.ui.sequence

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioseq.geneseqanalyzer.model.FastaRecord
import com.bioseq.geneseqanalyzer.repository.NcbiRepository
import com.bioseq.geneseqanalyzer.utils.Resource
import kotlinx.coroutines.launch

class SequenceViewModel(private val repository: NcbiRepository = NcbiRepository()) : ViewModel() {

    private val _sequenceState = MutableLiveData<Resource<FastaRecord>>()
    val sequenceState: LiveData<Resource<FastaRecord>> = _sequenceState

    fun fetchSequence(accessionId: String) {
        val cleanedId = accessionId.trim().uppercase()
        if (cleanedId.isEmpty()) {
            _sequenceState.value = Resource.Error("Accession ID cannot be empty.")
            return
        }

        _sequenceState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.fetchSequence(cleanedId)
            _sequenceState.value = result
        }
    }
}