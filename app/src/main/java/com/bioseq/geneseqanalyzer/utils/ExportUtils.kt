package com.bioseq.geneseqanalyzer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bioseq.geneseqanalyzer.model.AnalysisResult
import java.io.File
import java.util.Locale

object ExportUtils {

    fun buildAnalysisReportText(analysis: AnalysisResult): String {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("GENESEQ ANALYZER - MULTI-OMICS REPORT")
        sb.appendLine("==================================================")
        sb.appendLine("Accession ID : ${analysis.accessionId}")
        sb.appendLine("Description  : ${analysis.description}")
        sb.appendLine("Length       : ${analysis.length} bp")
        sb.appendLine("GC Content   : ${String.format(Locale.US, "%.2f", analysis.gcContent)}%")
        sb.appendLine("Melting Temp : ${String.format(Locale.US, "%.1f", analysis.meltingTemperature)} °C")
        sb.appendLine("Base Counts  : A=${analysis.baseCount.a}, T=${analysis.baseCount.t}, G=${analysis.baseCount.g}, C=${analysis.baseCount.c}")
        sb.appendLine()
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("REVERSE COMPLEMENT SEQUENCE")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine(analysis.reverseComplement)
        sb.appendLine()
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("PROTEIN TRANSLATION")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine(if (analysis.proteinTranslation.isNotEmpty()) analysis.proteinTranslation else "N/A")
        sb.appendLine()
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("OPEN READING FRAMES (${analysis.orfList.size} found)")
        sb.appendLine("--------------------------------------------------")

        if (analysis.orfList.isEmpty()) {
            sb.appendLine("No ORFs detected in sequence.")
        } else {
            for (orf in analysis.orfList) {
                sb.appendLine("${orf.id} | Strand: ${orf.strand} | Pos: ${orf.start}-${orf.end} | Len: ${orf.length} bp (${orf.protein.length} aa)")
                sb.appendLine("Protein: ${orf.protein}")
                sb.appendLine()
            }
        }

        sb.appendLine("--------------------------------------------------")
        sb.appendLine("ORIGINAL FASTA RECORD")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine(analysis.rawFasta)
        sb.appendLine("==================================================")

        return sb.toString()
    }

    fun shareAnalysis(context: Context, analysis: AnalysisResult) {
        val report = buildAnalysisReportText(analysis)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GeneSeq Report: ${analysis.accessionId}")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Analysis via"))
    }

    fun exportAnalysisToFile(context: Context, analysis: AnalysisResult) {
        try {
            val report = buildAnalysisReportText(analysis)
            val fileName = "GeneSeq_${analysis.accessionId.replace("[^a-zA-Z0-9]".toRegex(), "_")}.txt"
            val exportDir = File(context.getExternalFilesDir(null), "Reports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileName)
            file.writeText(report)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val openIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(openIntent, "Export Report File"))
            Toast.makeText(context, "Report exported to ${file.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}