package com.bioseq.geneseqanalyzer.utils

import java.util.regex.Pattern

object AccessionExtractor {

    // Regex patterns for RefSeq and GenBank NCBI Accessions
    private val ACCESSION_PATTERNS = listOf(
        // RefSeq: 2 letters + underscore + 6 to 9 digits (e.g. NM_000518, NC_000001, NP_000509)
        Pattern.compile("\\b([A-Z]{2}_\\d{6,9})\\b", Pattern.CASE_INSENSITIVE),
        // GenBank: 2 letters + 6 digits (e.g. AF123456, AY654321, DQ123456)
        Pattern.compile("\\b([A-Z]{2}\\d{6})\\b", Pattern.CASE_INSENSITIVE),
        // GenBank: 1 letter + 5 digits (e.g. U12345, M12345)
        Pattern.compile("\\b([A-Z]\\d{5})\\b", Pattern.CASE_INSENSITIVE)
    )

    fun extractAccessions(text: String): List<String> {
        val matches = LinkedHashSet<String>()
        val uppercaseText = text.uppercase()

        for (pattern in ACCESSION_PATTERNS) {
            val matcher = pattern.matcher(uppercaseText)
            while (matcher.find()) {
                matcher.group(1)?.let { matches.add(it) }
            }
        }

        return matches.toList()
    }
}