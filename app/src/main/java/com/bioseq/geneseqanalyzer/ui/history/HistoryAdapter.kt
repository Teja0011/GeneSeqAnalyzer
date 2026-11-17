package com.bioseq.geneseqanalyzer.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bioseq.geneseqanalyzer.data.local.SequenceEntity
import com.bioseq.geneseqanalyzer.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (SequenceEntity) -> Unit,
    private val onDeleteClick: (SequenceEntity) -> Unit
) : ListAdapter<SequenceEntity, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sequence: SequenceEntity) {
            binding.tvAccessionId.text = sequence.accessionId
            binding.tvDescription.text = sequence.description
            binding.tvLength.text = "${sequence.length} bp"
            binding.tvGcContent.text = String.format(Locale.US, "GC: %.1f%%", sequence.gcContent)
            binding.tvDate.text = dateFormat.format(Date(sequence.timestamp))

            binding.root.setOnClickListener {
                onItemClick(sequence)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(sequence)
            }
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<SequenceEntity>() {
        override fun areItemsTheSame(oldItem: SequenceEntity, newItem: SequenceEntity): Boolean {
            return oldItem.accessionId == newItem.accessionId
        }

        override fun areContentsTheSame(oldItem: SequenceEntity, newItem: SequenceEntity): Boolean {
            return oldItem == newItem
        }
    }
}