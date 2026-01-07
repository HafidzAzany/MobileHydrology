package com.example.projekuas.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projekuas.databinding.ItemTipBinding

class TipAdapter : RecyclerView.Adapter<TipAdapter.VH>() {

    private val items = mutableListOf<TipItem>()

    fun submit(list: List<TipItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(private val b: ItemTipBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: TipItem) {
            b.tvTitle.text = item.title
            b.tvDesc.text = item.desc
            b.tvTag.text = item.category
            b.tvRead.text = item.readLabel
        }
    }
}
