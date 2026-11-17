package com.bioseq.geneseqanalyzer.model

import biokt.NucleotideCount
import biokt.Orf

data class AnalysisResult(
    val accessionId: String,
    val header: String,
    val description: String,
    val rawFasta: String,
    val cleanSequence: String,
    val length: Int,
    val baseCount: NucleotideCount,
    val gcContent: Double,
    val reverseComplement: String,
    val proteinTranslation: String,
    val meltingTemperature: Double,
    val orfList: List<Orf>
)