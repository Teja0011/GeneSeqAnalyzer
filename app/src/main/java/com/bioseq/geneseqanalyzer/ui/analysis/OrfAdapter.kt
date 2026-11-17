package com.bioseq.geneseqanalyzer.ui.analysis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import biokt.Orf
import com.bioseq.geneseqanalyzer.databinding.ItemOrfBinding

class OrfAdapter(
    private val onCopyProteinClick: (Orf) -> Unit
) : ListAdapter<Orf, OrfAdapter.OrfViewHolder>(OrfDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrfViewHolder {
        val binding = ItemOrfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrfViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrfViewHolder(private val binding: ItemOrfBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orf: Orf) {
            binding.tvOrfName.text = orf.id
            binding.tvOrfStrand.text = "Strand: ${orf.strand}"
            binding.tvOrfPositions.text = "Location: ${orf.start} - ${orf.end} bp"
            binding.tvOrfLength.text = "Length: ${orf.length} bp (${orf.protein.length} aa)"
            binding.tvOrfProtein.text = if (orf.protein.isNotEmpty()) orf.protein else "No translation available"

            binding.btnCopyOrfProtein.setOnClickListener {
                onCopyProteinClick(orf)
            }
        }
    }

    class OrfDiffCallback : DiffUtil.ItemCallback<Orf>() {
        override fun areItemsTheSame(oldItem: Orf, newItem: Orf): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Orf, newItem: Orf): Boolean = oldItem == newItem
    }
}