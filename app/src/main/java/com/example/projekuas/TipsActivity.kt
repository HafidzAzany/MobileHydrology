package com.example.projekuas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projekuas.databinding.ActivityTipsBinding
import com.example.projekuas.tips.TipAdapter
import com.example.projekuas.tips.TipItem
import com.google.android.material.chip.Chip

class TipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsBinding
    private val adapter = TipAdapter()

    private val allTips = listOf(
        TipItem(
            title = "Awali Hari dengan Segelas Besar",
            desc = "Minum 500ml air putih setelah bangun tidur untuk menghidrasi tubuh.",
            category = "Sehari-hari",
            readLabel = "Baca"
        ),
        TipItem(
            title = "Minum Segelas Setiap 2 Jam",
            desc = "Atur alarm untuk minum air setiap 2 jam agar tubuh tetap terhidrasi.",
            category = "Praktis",
            readLabel = "Baca"
        ),
        TipItem(
            title = "Tips Saat Berolahraga",
            desc = "Minum 150–200ml setiap 15–20 menit saat olahraga agar stamina terjaga.",
            category = "Olahraga",
            readLabel = "Baca"
        )
    )

    private var selectedCategory: String = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Navbar global (layout_footer.xml)
        FooterManager.setupFooter(this)

        // Recycler
        binding.rvTips.layoutManager = LinearLayoutManager(this)
        binding.rvTips.adapter = adapter

        // default list
        applyFilter()

        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chips
        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            selectedCategory = chip?.text?.toString() ?: "Semua"
            applyFilter()
        }
    }

    private fun applyFilter() {
        val q = binding.etSearch.text?.toString()?.trim()?.lowercase() ?: ""

        val filtered = allTips.filter { tip ->
            val matchCategory = (selectedCategory == "Semua") || (tip.category == selectedCategory)
            val matchQuery = q.isEmpty() ||
                    tip.title.lowercase().contains(q) ||
                    tip.desc.lowercase().contains(q)

            matchCategory && matchQuery
        }

        adapter.submit(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
