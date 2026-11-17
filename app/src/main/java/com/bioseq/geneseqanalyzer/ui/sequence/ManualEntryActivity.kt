package com.bioseq.geneseqanalyzer.ui.sequence

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bioseq.geneseqanalyzer.R
import com.bioseq.geneseqanalyzer.databinding.ActivityManualEntryBinding
import com.bioseq.geneseqanalyzer.ui.analysis.AnalysisActivity
import com.bioseq.geneseqanalyzer.utils.Resource

class ManualEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualEntryBinding
    private val viewModel: SequenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSampleChips()
        setupListeners()
        observeViewModel()

        val initialAccession = intent.getStringExtra("EXTRA_ACCESSION_ID")
        if (!initialAccession.isNullOrBlank()) {
            binding.etAccession.setText(initialAccession)
            viewModel.fetchSequence(initialAccession)
        }
    }

    private fun setupSampleChips() {
        binding.chipSampleHbb.setOnClickListener {
            binding.etAccession.setText("NM_000518")
        }
        binding.chipSampleChr1.setOnClickListener {
            binding.etAccession.setText("NC_000001")
        }
        binding.chipSampleProt.setOnClickListener {
            binding.etAccession.setText("NP_000509")
        }
    }

    private fun setupListeners() {
        binding.btnFetchSequence.setOnClickListener {
            val accessionId = binding.etAccession.text.toString().trim()
            if (accessionId.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_accession), Toast.LENGTH_SHORT).show()
            } else {
                navigateToAnalysis(accessionId)
            }
        }

        binding.cardResult.setOnClickListener {
            val accessionId = binding.tvResultAccession.text.toString().trim()
            if (accessionId.isNotEmpty()) {
                navigateToAnalysis(accessionId)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.sequenceState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.layoutLoading.visibility = View.VISIBLE
                    binding.btnFetchSequence.isEnabled = false
                    binding.cardResult.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.btnFetchSequence.isEnabled = true
                    binding.cardResult.visibility = View.VISIBLE

                    val record = result.data
                    binding.tvResultAccession.text = record.accessionId
                    binding.tvResultDescription.text = record.description
                    binding.tvResultLength.text = "Sequence Length: ${record.length} bp (Tap to open full analysis)"
                    binding.tvFastaPreview.text = record.rawFasta
                }
                is Resource.Error -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.btnFetchSequence.isEnabled = true
                    binding.cardResult.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToAnalysis(accessionId: String) {
        val intent = Intent(this, AnalysisActivity::class.java).apply {
            putExtra("EXTRA_ACCESSION_ID", accessionId)
        }
        startActivity(intent)
    }
}