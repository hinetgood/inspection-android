package com.inspection.app.ui.cases

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inspection.app.data.entity.Case
import com.inspection.app.databinding.ItemCaseBinding

class CaseAdapter(
    private val onItemClick: (Case) -> Unit,
    private val onDeleteClick: (Case) -> Unit
) : ListAdapter<Case, CaseAdapter.CaseViewHolder>(CaseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding = ItemCaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CaseViewHolder(private val binding: ItemCaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(case: Case) {
            binding.tvCaseName.text = case.caseNumber
            binding.tvCaseDate.text = case.caseDate

            binding.root.setOnClickListener { onItemClick(case) }
            binding.btnDelete.setOnClickListener { onDeleteClick(case) }
        }
    }

    class CaseDiffCallback : DiffUtil.ItemCallback<Case>() {
        override fun areItemsTheSame(oldItem: Case, newItem: Case) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Case, newItem: Case) = oldItem == newItem
    }
}
