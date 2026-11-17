package com.bioseq.geneseqanalyzer.ui.history

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bioseq.geneseqanalyzer.data.local.SequenceEntity
import com.bioseq.geneseqanalyzer.databinding.ActivityHistoryBinding
import com.bioseq.geneseqanalyzer.ui.analysis.AnalysisActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { sequence ->
                val intent = Intent(this, AnalysisActivity::class.java).apply {
                    putExtra("EXTRA_ACCESSION_ID", sequence.accessionId)
                }
                startActivity(intent)
            },
            onDeleteClick = { sequence ->
                confirmDeletion(sequence)
            }
        )

        binding.rvSavedSequences.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.savedSequences.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvSavedSequences.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvSavedSequences.visibility = View.VISIBLE
                historyAdapter.submitList(list)
            }
        }
    }

    private fun confirmDeletion(sequence: SequenceEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Saved Sequence")
            .setMessage("Are you sure you want to delete ${sequence.accessionId} from your saved database?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteSequence(sequence)
                Toast.makeText(this, "Sequence ${sequence.accessionId} deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}