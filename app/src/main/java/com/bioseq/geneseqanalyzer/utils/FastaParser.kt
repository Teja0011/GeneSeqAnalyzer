package com.bioseq.geneseqanalyzer.utils

import com.bioseq.geneseqanalyzer.model.FastaRecord

object FastaParser {

    fun parseFasta(rawFasta: String, fallbackAccessionId: String): FastaRecord {
        val trimmedText = rawFasta.trim()
        if (trimmedText.isEmpty() || !trimmedText.startsWith(">")) {
            throw IllegalArgumentException("Invalid FASTA format received from NCBI.")
        }

        val lines = trimmedText.lines()
        val headerLine = lines.first().substring(1).trim()
        val sequenceBuilder = StringBuilder()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith(">")) break // Multiple FASTA records, stop at first
            if (line.isNotEmpty()) {
                sequenceBuilder.append(line.uppercase().replace("\\s+".toRegex(), ""))
            }
        }

        val cleanSequence = sequenceBuilder.toString()
        val headerParts = headerLine.split("\\s+".toRegex(), limit = 2)
        val extractedId = if (headerParts.isNotEmpty() && headerParts[0].isNotBlank()) headerParts[0] else fallbackAccessionId
        val description = if (headerParts.size > 1) headerParts[1] else "NCBI Sequence $extractedId"

        return FastaRecord(
            accessionId = extractedId,
            header = headerLine,
            description = description,
            rawFasta = trimmedText,
            cleanSequence = cleanSequence,
            length = cleanSequence.length
        )
    }
}