package com.example.projekuas

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projekuas.data.AppDatabase
import com.example.projekuas.databinding.ActivityHistoryBinding
import com.example.projekuas.history.DrinkLogAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: AppDatabase
    private val adapter = DrinkLogAdapter()

    private enum class SortMode { NEWEST, OLDEST, BIGGEST, SMALLEST }
    private var sortMode: SortMode = SortMode.NEWEST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ navbar reusable (JANGAN pakai setupBottomNav manual lagi)
        FooterManager.setupFooter(this)

        db = AppDatabase.getInstance(this)

        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter

        // Default: Today
        showSection(today = true)
        loadToday()

        // Chip click (cukup salah satu cara, jangan dobel listener)
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipToday -> {
                    showSection(today = true)
                    loadToday()
                }
                R.id.chipWeek -> {
                    showSection(week = true)
                    loadWeek()
                }
                R.id.chipMonth -> {
                    showSection(month = true)
                    loadMonthPlaceholder()
                }
            }
        }

        // Filter bottomsheet
        binding.btnFilter.setOnClickListener { showFilterBottomSheet() }
    }

    private fun showSection(today: Boolean = false, week: Boolean = false, month: Boolean = false) {
        binding.sectionToday.visibility = if (today) View.VISIBLE else View.GONE
        binding.sectionWeek.visibility = if (week) View.VISIBLE else View.GONE
        binding.sectionMonth.visibility = if (month) View.VISIBLE else View.GONE
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun keyDaysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun loadToday() {
        val key = todayKey()
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = db.drinkLogDao()

            val logs = when (sortMode) {
                SortMode.NEWEST -> dao.getByDateNewest(key)
                SortMode.OLDEST -> dao.getByDateOldest(key)
                SortMode.BIGGEST -> dao.getByDateBiggest(key)
                SortMode.SMALLEST -> dao.getByDateSmallest(key)
            }

            val total = dao.sumByDate(key)
            val progress = db.dailyProgressDao().getByDate(key)
            val target = progress?.targetMl ?: 2000
            val percent = if (target <= 0) 0 else (total * 100 / target).coerceIn(0, 100)

            withContext(Dispatchers.Main) {
                binding.tvTitle.text = "Riwayat Minum"
                binding.tvTotalTop.text = "$total ml"
                binding.tvTargetTop.text = "Target $target ml"
                binding.progressTop.progress = percent
                binding.tvPercentTop.text = "Terpenuhi $percent%"
                adapter.submit(logs)
            }
        }
    }

    private fun loadWeek() {
        val endKey = todayKey()
        val startKey = keyDaysAgo(6)

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = db.drinkLogDao()

            val logs = dao.getBetween(startKey, endKey)
            val total7 = dao.sumBetween(startKey, endKey)
            val daySums = dao.sumPerDayBetween(startKey, endKey)
            val map = daySums.associate { it.dateKey to it.totalMl }

            // urut 6 hari lalu -> hari ini (7 bar)
            val values = (6 downTo 0).map { daysAgo ->
                val key = keyDaysAgo(daysAgo)
                map[key] ?: 0
            }

            val avg = (total7 / 7.0).roundToInt()
            val targetDaily = 2000
            val hitDays = values.count { it >= targetDaily }

            // top card tetap menampilkan today (sesuai UI kamu)
            val today = todayKey()
            val todayTotal = dao.sumByDate(today)
            val todayTarget = db.dailyProgressDao().getByDate(today)?.targetMl ?: 2000
            val percent = if (todayTarget <= 0) 0 else (todayTotal * 100 / todayTarget).coerceIn(0, 100)

            withContext(Dispatchers.Main) {
                binding.tvTitle.text = "Riwayat Minum"

                binding.tvTotalTop.text = "$todayTotal ml"
                binding.tvTargetTop.text = "Target $todayTarget ml"
                binding.progressTop.progress = percent
                binding.tvPercentTop.text = "Terpenuhi $percent%"

                binding.tvWeekTotal.text = total7.toString()
                binding.tvWeekAvg.text = avg.toString()
                binding.tvWeekTargetDays.text = "$hitDays/7"

                renderBars(values)

                // kalau kamu mau list minggu ini tampil juga, biarin:
                adapter.submit(logs)
            }
        }
    }

    private fun loadMonthPlaceholder() {
        // cuma placeholder, ga query database
        binding.tvTitle.text = "Riwayat Minum"
    }

    private fun renderBars(values: List<Int>) {
        val container = binding.chartBars
        container.removeAllViews()

        val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
        val maxHeightPx = dpToPx(160)

        for (i in 0 until 7) {
            val v = values.getOrElse(i) { 0 }
            val h = ((v.toFloat() / max) * maxHeightPx).toInt().coerceAtLeast(dpToPx(6))

            val barWrapper = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginStart = dpToPx(6)
                    marginEnd = dpToPx(6)
                }
                gravity = Gravity.BOTTOM
            }

            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    h
                )
                setBackgroundColor(getColor(R.color.text_color_secondary))
                alpha = 0.6f
            }

            barWrapper.addView(bar)
            container.addView(barWrapper)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).roundToInt()

    private fun showFilterBottomSheet() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_filter_history, null)
        dialog.setContentView(view)

        val btnClose = view.findViewById<android.widget.ImageButton>(R.id.btnClose)

        val btnNewest = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNewest)
        val btnOldest = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnOldest)
        val btnBiggest = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBiggest)
        val btnSmallest = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSmallest)

        fun applySelectedUI() {
            btnNewest.icon = null
            btnOldest.icon = null
            btnBiggest.icon = null
            btnSmallest.icon = null

            val check = R.drawable.ic_check
            when (sortMode) {
                SortMode.NEWEST -> btnNewest.setIconResource(check)
                SortMode.OLDEST -> btnOldest.setIconResource(check)
                SortMode.BIGGEST -> btnBiggest.setIconResource(check)
                SortMode.SMALLEST -> btnSmallest.setIconResource(check)
            }
        }

        applySelectedUI()

        btnNewest.setOnClickListener { sortMode = SortMode.NEWEST; loadToday(); dialog.dismiss() }
        btnOldest.setOnClickListener { sortMode = SortMode.OLDEST; loadToday(); dialog.dismiss() }
        btnBiggest.setOnClickListener { sortMode = SortMode.BIGGEST; loadToday(); dialog.dismiss() }
        btnSmallest.setOnClickListener { sortMode = SortMode.SMALLEST; loadToday(); dialog.dismiss() }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
