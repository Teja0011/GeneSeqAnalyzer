package com.bioseq.geneseqanalyzer.ui.analysis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import biokt.Orf
import com.bioseq.geneseqanalyzer.data.local.AppDatabase
import com.bioseq.geneseqanalyzer.data.local.SequenceEntity
import com.bioseq.geneseqanalyzer.databinding.ActivityAnalysisBinding
import com.bioseq.geneseqanalyzer.model.AnalysisResult
import com.bioseq.geneseqanalyzer.repository.SavedSequenceRepository
import com.bioseq.geneseqanalyzer.utils.ExportUtils
import com.bioseq.geneseqanalyzer.utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private val viewModel: AnalysisViewModel by viewModels()
    private lateinit var orfAdapter: OrfAdapter
    private var currentAnalysisResult: AnalysisResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        val accessionId = intent.getStringExtra("EXTRA_ACCESSION_ID")
        if (!accessionId.isNullOrBlank()) {
            viewModel.analyzeAccession(accessionId)
        } else {
            Toast.makeText(this, "No Accession ID provided for analysis", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        orfAdapter = OrfAdapter { orf ->
            copyToClipboard("ORF Protein (${orf.id})", orf.protein)
        }
        binding.rvOrfs.apply {
            layoutManager = LinearLayoutManager(this@AnalysisActivity)
            adapter = orfAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSaveSequence.setOnClickListener {
            currentAnalysisResult?.let { result ->
                saveSequenceToDatabase(result)
            }
        }

        binding.btnShareAnalysis.setOnClickListener {
            currentAnalysisResult?.let { result ->
                ExportUtils.shareAnalysis(this, result)
            }
        }

        binding.btnExportAnalysis.setOnClickListener {
            currentAnalysisResult?.let { result ->
                ExportUtils.exportAnalysisToFile(this, result)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.analysisState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.layoutLoading.visibility = View.VISIBLE
                    binding.layoutContent.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.layoutContent.visibility = View.VISIBLE
                    currentAnalysisResult = result.data
                    displayAnalysisData(result.data)
                }
                is Resource.Error -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.layoutContent.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayAnalysisData(analysis: AnalysisResult) {
        // Header
        binding.tvAccessionHeader.text = analysis.accessionId
        binding.tvDescriptionHeader.text = analysis.description

        // Details Card
        binding.tvSeqLength.text = "Sequence Length: ${analysis.length} bp"
        binding.tvRawFasta.text = analysis.rawFasta

        // Base Composition Card
        val gcFormatted = String.format(Locale.US, "GC Content: %.2f%%", analysis.gcContent)
        binding.tvGcContent.text = gcFormatted
        binding.tvCountA.text = "A: ${analysis.baseCount.a}"
        binding.tvCountT.text = "T: ${analysis.baseCount.t}"
        binding.tvCountG.text = "G: ${analysis.baseCount.g}"
        binding.tvCountC.text = "C: ${analysis.baseCount.c}"

        // Properties Card
        val tmFormatted = String.format(Locale.US, "Melting Temp (Tm): %.1f °C", analysis.meltingTemperature)
        binding.tvMeltingTemp.text = tmFormatted
        binding.tvRevComp.text = analysis.reverseComplement
        binding.tvProteinTranslation.text = if (analysis.proteinTranslation.isNotEmpty()) analysis.proteinTranslation else "N/A"

        // ORF Card
        binding.tvOrfHeader.text = "Open Reading Frames (${analysis.orfList.size} found)"
        orfAdapter.submitList(analysis.orfList)

        // Copy Click Handlers
        binding.btnCopyFasta.setOnClickListener {
            copyToClipboard("FASTA Sequence", analysis.rawFasta)
        }
        binding.btnCopyRevComp.setOnClickListener {
            copyToClipboard("Reverse Complement", analysis.reverseComplement)
        }
        binding.btnCopyProtein.setOnClickListener {
            copyToClipboard("Protein Translation", analysis.proteinTranslation)
        }
    }

    private fun saveSequenceToDatabase(analysis: AnalysisResult) {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SavedSequenceRepository(database.sequenceDao())
        lifecycleScope.launch {
            val entity = SequenceEntity(
                accessionId = analysis.accessionId,
                description = analysis.description,
                rawFasta = analysis.rawFasta,
                cleanSequence = analysis.cleanSequence,
                length = analysis.length,
                gcContent = analysis.gcContent,
                timestamp = System.currentTimeMillis()
            )
            repository.saveSequence(entity)
            Toast.makeText(this@AnalysisActivity, "Sequence ${analysis.accessionId} saved to database!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        if (text.isEmpty()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}