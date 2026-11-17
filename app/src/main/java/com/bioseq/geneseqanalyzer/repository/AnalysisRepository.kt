package com.bioseq.geneseqanalyzer.repository

import biokt.DNASequence
import biokt.SeqIO
import com.bioseq.geneseqanalyzer.model.AnalysisResult
import com.bioseq.geneseqanalyzer.model.FastaRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalysisRepository {

    suspend fun analyzeFastaRecord(fastaRecord: FastaRecord): AnalysisResult {
        return withContext(Dispatchers.Default) {
            val seqRecords = SeqIO.parseString(fastaRecord.rawFasta, "fasta")
            val dna = if (seqRecords.isNotEmpty()) seqRecords.first() else DNASequence(fastaRecord.cleanSequence, fastaRecord.accessionId)

            val baseCounts = dna.countBases()
            val gc = dna.gcContent()
            val revComp = dna.reverseComplement().cleanSeq
            val protein = dna.translate()
            val tm = dna.meltingTemperature()
            val orfs = dna.findOrfs(minLength = 100)

            AnalysisResult(
                accessionId = fastaRecord.accessionId,
                header = fastaRecord.header,
                description = fastaRecord.description,
                rawFasta = fastaRecord.rawFasta,
                cleanSequence = dna.cleanSeq,
                length = dna.length,
                baseCount = baseCounts,
                gcContent = gc,
                reverseComplement = revComp,
                proteinTranslation = protein,
                meltingTemperature = tm,
                orfList = orfs
            )
        }
    }
}