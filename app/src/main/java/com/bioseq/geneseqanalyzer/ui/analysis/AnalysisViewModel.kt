package com.bioseq.geneseqanalyzer.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioseq.geneseqanalyzer.model.AnalysisResult
import com.bioseq.geneseqanalyzer.model.FastaRecord
import com.bioseq.geneseqanalyzer.repository.AnalysisRepository
import com.bioseq.geneseqanalyzer.repository.NcbiRepository
import com.bioseq.geneseqanalyzer.utils.Resource
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val ncbiRepository: NcbiRepository = NcbiRepository(),
    private val analysisRepository: AnalysisRepository = AnalysisRepository()
) : ViewModel() {

    private val _analysisState = MutableLiveData<Resource<AnalysisResult>>()
    val analysisState: LiveData<Resource<AnalysisResult>> = _analysisState

    fun analyzeAccession(accessionId: String) {
        _analysisState.value = Resource.Loading
        viewModelScope.launch {
            when (val fetchResult = ncbiRepository.fetchSequence(accessionId)) {
                is Resource.Success -> {
                    val analysis = analysisRepository.analyzeFastaRecord(fetchResult.data)
                    _analysisState.value = Resource.Success(analysis)
                }
                is Resource.Error -> {
                    _analysisState.value = Resource.Error(fetchResult.message, fetchResult.cause)
                }
                is Resource.Loading -> {
                    _analysisState.value = Resource.Loading
                }
            }
        }
    }

    fun analyzeDirectFasta(fastaRecord: FastaRecord) {
        _analysisState.value = Resource.Loading
        viewModelScope.launch {
            val analysis = analysisRepository.analyzeFastaRecord(fastaRecord)
            _analysisState.value = Resource.Success(analysis)
        }
    }
}