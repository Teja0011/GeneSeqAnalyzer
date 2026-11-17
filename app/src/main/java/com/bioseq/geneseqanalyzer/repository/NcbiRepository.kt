package com.bioseq.geneseqanalyzer.repository

import com.bioseq.geneseqanalyzer.data.remote.NcbiApiService
import com.bioseq.geneseqanalyzer.data.remote.NcbiRetrofitClient
import com.bioseq.geneseqanalyzer.model.FastaRecord
import com.bioseq.geneseqanalyzer.utils.FastaParser
import com.bioseq.geneseqanalyzer.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NcbiRepository(private val apiService: NcbiApiService = NcbiRetrofitClient.apiService) {

    suspend fun fetchSequence(accessionId: String): Resource<FastaRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.fetchFastaSequence(accessionId = accessionId.trim())
                if (response.isSuccessful) {
                    val rawBody = response.body()
                    if (!rawBody.isNullOrBlank()) {
                        try {
                            val fastaRecord = FastaParser.parseFasta(rawBody, accessionId)
                            Resource.Success(fastaRecord)
                        } catch (e: Exception) {
                            Resource.Error("Invalid FASTA format returned for $accessionId: ${e.localizedMessage}")
                        }
                    } else {
                        Resource.Error("Empty response received from NCBI for accession $accessionId.")
                    }
                } else {
                    Resource.Error("NCBI E-utilities API error (HTTP ${response.code()}): ${response.message()}")
                }
            } catch (e: UnknownHostException) {
                Resource.Error("Network error: Please check your internet connection.", e)
            } catch (e: SocketTimeoutException) {
                Resource.Error("Network timeout: NCBI server did not respond in time.", e)
            } catch (e: Exception) {
                Resource.Error("Failed to retrieve sequence: ${e.localizedMessage ?: "Unknown error"}", e)
            }
        }
    }
}