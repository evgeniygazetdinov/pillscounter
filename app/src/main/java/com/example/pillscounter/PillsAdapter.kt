package com.example.pillscounter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pillscounter.data.Pill

class PillsAdapter(
    private val onItemClick: (Pill) -> Unit,
    private val onItemLongClick: (Pill) -> Unit
) : ListAdapter<Pill, PillsAdapter.PillViewHolder>(PillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pill, parent, false)
        return PillViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PillViewHolder(
        itemView: View,
        private val onItemClick: (Pill) -> Unit,
        private val onItemLongClick: (Pill) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val pillImage: ImageView = itemView.findViewById(R.id.pillImage)
        private val pillName: TextView = itemView.findViewById(R.id.pillName)
        private val pillDetails: TextView = itemView.findViewById(R.id.pillDetails)
        private var currentPill: Pill? = null

        init {
            itemView.setOnClickListener {
                currentPill?.let(onItemClick)
            }
            itemView.setOnLongClickListener {
                currentPill?.let(onItemLongClick)
                true
            }
        }

        fun bind(pill: Pill) {
            currentPill = pill
            pillName.text = pill.name
            pillDetails.text = itemView.context.getString(
                R.string.dosage_format,
                "${pill.dosage} - ${itemView.context.getString(R.string.remaining, pill.totalCount)}"
            )

            pill.imageUri?.let { uri ->
                pillImage.setImageURI(Uri.parse(uri))
            } ?: run {
                pillImage.setImageResource(R.drawable.ic_pill_placeholder)
            }
        }
    }

    private class PillDiffCallback : DiffUtil.ItemCallback<Pill>() {
        override fun areItemsTheSame(oldItem: Pill, newItem: Pill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pill, newItem: Pill): Boolean {
            return oldItem == newItem
        }
    }
}
