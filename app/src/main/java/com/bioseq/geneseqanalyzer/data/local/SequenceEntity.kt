package com.bioseq.geneseqanalyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_sequences")
data class SequenceEntity(
    @PrimaryKey val accessionId: String,
    val description: String,
    val rawFasta: String,
    val cleanSequence: String,
    val length: Int,
    val gcContent: Double,
    val timestamp: Long = System.currentTimeMillis()
)