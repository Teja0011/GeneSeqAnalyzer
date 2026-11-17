package biokt

object IUPAC {
    val AMBIGUOUS_DNA: Map<Char, List<Char>> = mapOf(
        'R' to listOf('A', 'G'),
        'Y' to listOf('C', 'T'),
        'S' to listOf('G', 'C'),
        'W' to listOf('A', 'T'),
        'K' to listOf('G', 'T'),
        'M' to listOf('A', 'C'),
        'B' to listOf('C', 'G', 'T'),
        'D' to listOf('A', 'G', 'T'),
        'H' to listOf('A', 'C', 'T'),
        'V' to listOf('A', 'C', 'G'),
        'N' to listOf('A', 'C', 'G', 'T')
    )
}

object RestrictionEnzymes {
    val COMMON_ENZYMES: Map<String, String> = mapOf(
        "EcoRI" to "GAATTC",
        "BamHI" to "GGATCC",
        "HindIII" to "AAGCTT",
        "TaqI" to "TCGA",
        "NotI" to "GCGGCCGC",
        "XhoI" to "CTCGAG"
    )

    fun findCutSites(seq: String, enzymeName: String): List<Int> {
        val motif = COMMON_ENZYMES[enzymeName] ?: return emptyList()
        val cuts = mutableListOf<Int>()
        var index = seq.indexOf(motif, 0, ignoreCase = true)
        while (index >= 0) {
            cuts.add(index)
            index = seq.indexOf(motif, index + 1, ignoreCase = true)
        }
        return cuts
    }
}

object SeqStats {
    fun calculateGC(seq: String): Double {
        return DNASequence(seq).gcContent()
    }

    fun calculateMeltingTemp(seq: String): Double {
        return DNASequence(seq).meltingTemperature()
    }
}