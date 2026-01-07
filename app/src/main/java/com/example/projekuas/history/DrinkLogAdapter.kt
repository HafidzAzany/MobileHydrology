package com.example.projekuas.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projekuas.R
import com.example.projekuas.data.DrinkLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrinkLogAdapter : RecyclerView.Adapter<DrinkLogAdapter.VH>() {

    private val items = mutableListOf<DrinkLogEntity>()
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submit(list: List<DrinkLogEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_drink_log, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], timeFmt)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvSource = itemView.findViewById<TextView>(R.id.tvSource)
        private val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)

        fun bind(item: DrinkLogEntity, fmt: SimpleDateFormat) {
            tvTime.text = fmt.format(Date(item.timeMillis))
            tvAmount.text = "${item.amountMl} ml"
            tvSource.text = if (item.source == "auto") "Dicatat otomatis" else "Ditambahkan manual"
        }
    }
}
