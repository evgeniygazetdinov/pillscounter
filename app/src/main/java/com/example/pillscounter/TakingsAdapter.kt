package com.example.pillscounter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pillscounter.data.PillTaking
import java.text.SimpleDateFormat
import java.util.Locale

class TakingsAdapter(
    private val onEditClick: (PillTaking) -> Unit,
    private val onDeleteClick: (PillTaking) -> Unit
) : ListAdapter<PillTaking, TakingsAdapter.TakingViewHolder>(TakingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TakingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_taking, parent, false)
        return TakingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TakingViewHolder, position: Int) {
        val taking = getItem(position)
        holder.bind(taking)
    }

    inner class TakingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val countText: TextView = itemView.findViewById(R.id.countText)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        fun bind(taking: PillTaking) {
            timeText.text = dateFormat.format(taking.timestamp)
            countText.text = itemView.context.getString(R.string.taking_count, taking.count)

            editButton.setOnClickListener { onEditClick(taking) }
            deleteButton.setOnClickListener { onDeleteClick(taking) }
        }
    }

    private class TakingDiffCallback : DiffUtil.ItemCallback<PillTaking>() {
        override fun areItemsTheSame(oldItem: PillTaking, newItem: PillTaking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PillTaking, newItem: PillTaking): Boolean {
            return oldItem == newItem
        }
    }
}
