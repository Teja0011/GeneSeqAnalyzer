package biokt

object SeqIO {

    fun parseString(text: String, format: String = "fasta"): List<DNASequence> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()

        val records = mutableListOf<DNASequence>()
        val blocks = trimmed.split("(?=\\n>)".toRegex()).map { it.trim() }.filter { it.isNotEmpty() }

        for (block in blocks) {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            val headerLine = if (lines[0].startsWith(">")) lines[0].substring(1).trim() else lines[0]
            val seqBuilder = StringBuilder()

            for (i in 1 until lines.size) {
                if (!lines[i].startsWith(">")) {
                    seqBuilder.append(lines[i].uppercase().replace("\\s+".toRegex(), ""))
                }
            }

            records.add(DNASequence(seqBuilder.toString(), id = headerLine))
        }

        return records
    }
}