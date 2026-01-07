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

        db = AppDatabase.getInstance(this)

        setupBottomNav(active = "history")

        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter

        // Default: Hari ini
        showSection(today = true)
        loadToday()

        binding.chipToday.setOnClickListener {
            binding.chipToday.isChecked = true
            showSection(today = true)
            loadToday()
        }

        binding.chipWeek.setOnClickListener {
            binding.chipWeek.isChecked = true
            showSection(week = true)
            loadWeek()
        }

        binding.chipMonth.setOnClickListener {
            binding.chipMonth.isChecked = true
            showSection(month = true)
            loadMonthPlaceholder()
        }

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

        // FILTER
        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }
    }

    private fun setupBottomNav(active: String) {
        val bottom = findViewById<android.view.View>(R.id.bottomNav)

        val navHome = bottom.findViewById<android.view.View>(R.id.navHome)
        val navHistory = bottom.findViewById<android.view.View>(R.id.navHistory)
        val navTips = bottom.findViewById<android.view.View>(R.id.navTips)
        val navSettings = bottom.findViewById<android.view.View>(R.id.navSettings)

        // Klik nav
        navHome.setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }

        navHistory.setOnClickListener {
            // sudah di sini gak usah ngapa-ngapain
        }

        navTips.setOnClickListener {
            // ganti TipsActivity
            // startActivity(Intent(this, TipsActivity::class.java))
        }

        navSettings.setOnClickListener {
            // ganti SettingsActivity
            // startActivity(Intent(this, SettingsActivity::class.java))
        }

        // highlight tab aktif (opsional tapi enak)
        val cyan = getColor(R.color.cyan_accent)
        val gray = getColor(R.color.text_color_secondary)

        fun setActive(iconId: Int, textId: Int, isActive: Boolean) {
            bottom.findViewById<android.widget.ImageView>(iconId).setColorFilter(if (isActive) cyan else gray)
            bottom.findViewById<android.widget.TextView>(textId).setTextColor(if (isActive) cyan else gray)
        }

        setActive(R.id.iconHome, R.id.textHome, active == "home")
        setActive(R.id.iconHistory, R.id.textHistory, active == "history")
        setActive(R.id.iconTips, R.id.textTips, active == "com/example/projekuas/tips")
        setActive(R.id.iconSettings, R.id.textSettings, active == "settings")
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
        val startKey = keyDaysAgo(6) // 7 hari termasuk hari ini

        lifecycleScope.launch(Dispatchers.IO) {
            // list log untuk ditampilkan di recycler (opsional, tetap kita tampilkan biar ada isinya)
            val logs = db.drinkLogDao().getBetween(startKey, endKey)

            // total 7 hari
            val total7 = db.drinkLogDao().sumBetween(startKey, endKey)

            // sum per hari untuk bar chart
            val daySums = db.drinkLogDao().sumPerDayBetween(startKey, endKey)

            // jadikan map biar gampang ambil per dateKey
            val map = daySums.associateBy({ it.dateKey }, { it.totalMl })

            // bikin list 7 hari urut dari 6 hari lalu -> hari ini
            val values = (6 downTo 0).map { daysAgo ->
                val key = keyDaysAgo(daysAgo)
                map[key] ?: 0
            }

            val avg = (total7 / 7.0).roundToInt()

            // target days: berapa hari yang tembus target harian (ambil dari daily_progress hari itu kalau ada)
            // biar simpel & aman: pakai target default 2000
            val targetDaily = 2000
            val hitDays = values.count { it >= targetDaily }

            withContext(Dispatchers.Main) {
                binding.tvTitle.text = "Riwayat Minum"

                // Card top tetap “Total Hari Ini” sesuai figma, tapi kamu bisa ubah teksnya:
                binding.tvTotalTop.text = "${db.drinkLogDao().sumByDate(todayKey())} ml"
                binding.tvTargetTop.text = "Target ${db.dailyProgressDao().getByDate(todayKey())?.targetMl ?: 2000} ml"
                val todayTotal = db.drinkLogDao().sumByDate(todayKey())
                val todayTarget = db.dailyProgressDao().getByDate(todayKey())?.targetMl ?: 2000
                val percent = if (todayTarget <= 0) 0 else (todayTotal * 100 / todayTarget).coerceIn(0, 100)
                binding.progressTop.progress = percent
                binding.tvPercentTop.text = "Terpenuhi $percent%"

                // 3 cards minggu ini
                binding.tvWeekTotal.text = total7.toString()
                binding.tvWeekAvg.text = avg.toString()
                binding.tvWeekTargetDays.text = "$hitDays/7"

                // chart
                renderBars(values)

                // recycler: tampilkan logs 7 hari (boleh)
                adapter.submit(logs)
            }
        }
    }

    private fun loadMonthPlaceholder() {
        // Sesuai figma kamu: cuma placeholder "fitur dalam pengembangan"
        binding.tvTitle.text = "Riwayat Minum"
        // tidak perlu query apa-apa
    }

    private fun renderBars(values: List<Int>) {
        // values harus 7 (Sen..Min tampilan di bawah itu label fix, jadi kita fokus tampilkan 7 bar)
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
            // reset icon semua dulu
            btnNewest.icon = null
            btnOldest.icon = null
            btnBiggest.icon = null
            btnSmallest.icon = null

            // kasih centang ke yang aktif sekarang
            val check = R.drawable.ic_check
            when (sortMode) {
                SortMode.NEWEST -> btnNewest.setIconResource(check)
                SortMode.OLDEST -> btnOldest.setIconResource(check)
                SortMode.BIGGEST -> btnBiggest.setIconResource(check)
                SortMode.SMALLEST -> btnSmallest.setIconResource(check)
            }
        }

        applySelectedUI()

        btnNewest.setOnClickListener {
            sortMode = SortMode.NEWEST
            applySelectedUI()
            loadToday()
            dialog.dismiss()
        }
        btnOldest.setOnClickListener {
            sortMode = SortMode.OLDEST
            applySelectedUI()
            loadToday()
            dialog.dismiss()
        }
        btnBiggest.setOnClickListener {
            sortMode = SortMode.BIGGEST
            applySelectedUI()
            loadToday()
            dialog.dismiss()
        }
        btnSmallest.setOnClickListener {
            sortMode = SortMode.SMALLEST
            applySelectedUI()
            loadToday()
            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

}
