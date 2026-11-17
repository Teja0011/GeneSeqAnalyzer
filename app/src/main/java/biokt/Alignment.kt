package biokt

data class AlignmentResult(
    val seq1Aligned: String,
    val seq2Aligned: String,
    val score: Double,
    val identity: Double
)

object PairwiseAligner {

    fun globalAlign(seq1: String, seq2: String, matchScore: Double = 1.0, mismatchPenalty: Double = -1.0, gapPenalty: Double = -1.0): AlignmentResult {
        val s1 = seq1.uppercase()
        val s2 = seq2.uppercase()
        val n = s1.length
        val m = s2.length

        val dp = Array(n + 1) { DoubleArray(m + 1) }

        for (i in 0..n) dp[i][0] = i * gapPenalty
        for (j in 0..m) dp[0][j] = j * gapPenalty

        for (i in 1..n) {
            for (j in 1..m) {
                val scoreMatch = dp[i - 1][j - 1] + (if (s1[i - 1] == s2[j - 1]) matchScore else mismatchPenalty)
                val scoreDelete = dp[i - 1][j] + gapPenalty
                val scoreInsert = dp[i][j - 1] + gapPenalty
                dp[i][j] = maxOf(scoreMatch, scoreDelete, scoreInsert)
            }
        }

        var i = n
        var j = m
        val align1 = StringBuilder()
        val align2 = StringBuilder()
        var matches = 0

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + (if (s1[i - 1] == s2[j - 1]) matchScore else mismatchPenalty)) {
                align1.append(s1[i - 1])
                align2.append(s2[j - 1])
                if (s1[i - 1] == s2[j - 1]) matches++
                i--
                j--
            } else if (i > 0 && (j == 0 || dp[i][j] == dp[i - 1][j] + gapPenalty)) {
                align1.append(s1[i - 1])
                align2.append('-')
                i--
            } else {
                align1.append('-')
                align2.append(s2[j - 1])
                j--
            }
        }

        val alignedStr1 = align1.reverse().toString()
        val alignedStr2 = align2.reverse().toString()
        val totalLen = alignedStr1.length
        val identity = if (totalLen > 0) (matches.toDouble() / totalLen.toDouble()) * 100.0 else 0.0

        return AlignmentResult(alignedStr1, alignedStr2, dp[n][m], identity)
    }
}