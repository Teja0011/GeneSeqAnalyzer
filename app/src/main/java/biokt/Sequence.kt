package biokt

data class Orf(
    val id: String,
    val start: Int,
    val end: Int,
    val strand: String,
    val sequence: String,
    val protein: String,
    val length: Int
)

data class NucleotideCount(
    val a: Int,
    val t: Int,
    val g: Int,
    val c: Int,
    val n: Int,
    val total: Int,
    val gcPercentage: Double
)

class DNASequence(val sequence: String, val id: String = "") {

    val cleanSeq: String = sequence.uppercase().replace("[^ATCGN]".toRegex(), "")
    val length: Int get() = cleanSeq.length

    fun countBases(): NucleotideCount {
        var a = 0
        var t = 0
        var g = 0
        var c = 0
        var n = 0

        for (ch in cleanSeq) {
            when (ch) {
                'A' -> a++
                'T' -> t++
                'G' -> g++
                'C' -> c++
                else -> n++
            }
        }

        val total = cleanSeq.length
        val gc = if (total > 0) ((g + c).toDouble() / total.toDouble()) * 100.0 else 0.0

        return NucleotideCount(a, t, g, c, n, total, gc)
    }

    fun gcContent(): Double {
        return countBases().gcPercentage
    }

    fun reverseComplement(): DNASequence {
        val sb = StringBuilder(cleanSeq.length)
        for (i in cleanSeq.length - 1 downTo 0) {
            val comp = when (cleanSeq[i]) {
                'A' -> 'T'
                'T' -> 'A'
                'G' -> 'C'
                'C' -> 'G'
                else -> 'N'
            }
            sb.append(comp)
        }
        return DNASequence(sb.toString(), id)
    }

    fun translate(): String {
        val sb = StringBuilder()
        for (i in 0 until cleanSeq.length - 2 step 3) {
            val codon = cleanSeq.substring(i, i + 3)
            val aa = CodonTable.translateCodon(codon)
            sb.append(aa)
        }
        return sb.toString()
    }

    fun meltingTemperature(): Double {
        val counts = countBases()
        val total = counts.total
        if (total == 0) return 0.0

        return if (total < 14) {
            // Wallace Rule for short primers
            (2.0 * (counts.a + counts.t)) + (4.0 * (counts.g + counts.c))
        } else {
            // Marmur and Doty formula for longer sequences
            64.9 + (41.0 * (counts.g + counts.c - 16.4) / total.toDouble())
        }
    }

    fun findOrfs(minLength: Int = 100): List<Orf> {
        val orfs = mutableListOf<Orf>()
        var index = 1

        // Forward strand (+1, +2, +3)
        for (frame in 0..2) {
            scanFrameForOrfs(cleanSeq, "+", frame, minLength) { start, end, seq, prot ->
                orfs.add(Orf("ORF #$index (+)", start + 1, end, "+", seq, prot, seq.length))
                index++
            }
        }

        // Reverse strand (-1, -2, -3)
        val revCompSeq = reverseComplement().cleanSeq
        for (frame in 0..2) {
            scanFrameForOrfs(revCompSeq, "-", frame, minLength) { start, end, seq, prot ->
                val realStart = length - end + 1
                val realEnd = length - start
                orfs.add(Orf("ORF #$index (-)", realStart, realEnd, "-", seq, prot, seq.length))
                index++
            }
        }

        return orfs
    }

    private inline fun scanFrameForOrfs(
        seqStr: String,
        strand: String,
        frame: Int,
        minLength: Int,
        onFound: (start: Int, end: Int, seq: String, prot: String) -> Unit
    ) {
        var startPos = -1
        for (i in frame until seqStr.length - 2 step 3) {
            val codon = seqStr.substring(i, i + 3)
            if (startPos == -1 && CodonTable.isStartCodon(codon)) {
                startPos = i
            } else if (startPos != -1 && CodonTable.isStopCodon(codon)) {
                val endPos = i + 3
                val orfSeq = seqStr.substring(startPos, endPos)
                if (orfSeq.length >= minLength) {
                    val protein = DNASequence(orfSeq).translate()
                    onFound(startPos, endPos, orfSeq, protein)
                }
                startPos = -1
            }
        }
    }
}