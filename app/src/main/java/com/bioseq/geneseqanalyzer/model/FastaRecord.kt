package com.bioseq.geneseqanalyzer.model

data class FastaRecord(
    val accessionId: String,
    val header: String,
    val description: String,
    val rawFasta: String,
    val cleanSequence: String,
    val length: Int
)