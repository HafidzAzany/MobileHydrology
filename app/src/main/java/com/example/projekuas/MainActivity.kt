package com.example.projekuas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dateRunnable: Runnable

    // progress minum harian
    private var targetMl = 2000
    private var currentMl = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startRealTimeDate()
        loadDailyProgress()
        updateUI()

        // tombol tambah minum
        binding.btnAddDrink.setOnClickListener {
            showAddDrinkBottomSheet()
        }

        // klik nav (sementara)
        binding.navHistory.setOnClickListener {
            // nanti: HistoryActivity
        }
        binding.navTips.setOnClickListener {
            // nanti: TipsActivity
        }
        binding.navSettings.setOnClickListener {
            // nanti: SettingsActivity
        }
    }

    private fun startRealTimeDate() {
        dateRunnable = object : Runnable {
            override fun run() {
                // Format: Hari, Tanggal Bulan Tahun Jam:Menit:Detik (Bahasa Indonesia)
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale("id", "ID"))
                binding.tvDate.text = sdf.format(Date())

                // Update setiap 1 detik
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(dateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dateRunnable)
    }

    private fun loadDailyProgress() {
        val prefs = getSharedPreferences("daily_progress", MODE_PRIVATE)

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString("last_date", null)

        // kalau hari berubah → reset current
        if (lastDate == null || lastDate != todayKey) {
            targetMl = prefs.getInt("target_ml", 2000)
            currentMl = 0

            prefs.edit()
                .putString("last_date", todayKey)
                .putInt("current_ml", currentMl)
                .apply()
        } else {
            targetMl = prefs.getInt("target_ml", 2000)
            currentMl = prefs.getInt("current_ml", 0)
        }
    }

    private fun saveDailyProgress() {
        val prefs = getSharedPreferences("daily_progress", MODE_PRIVATE)
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        prefs.edit()
            .putString("last_date", todayKey)
            .putInt("target_ml", targetMl)
            .putInt("current_ml", currentMl)
            .apply()
    }

    private fun updateUI() {
        val safeTarget = if (targetMl <= 0) 2000 else targetMl
        val safeCurrent = currentMl.coerceIn(0, safeTarget)

        val percent = (safeCurrent * 100 / safeTarget).coerceIn(0, 100)

        binding.tvCurrentMl.text = safeCurrent.toString()
        binding.tvSubMl.text = "ml / $safeTarget ml"
        binding.tvPercent.text = "$percent%"

        binding.tvTargetMl.text = "$safeTarget ml"

        val remaining = (safeTarget - safeCurrent).coerceAtLeast(0)
        binding.tvRemainingMl.text = "$remaining ml"

        binding.progressRing.progress = percent
    }

    private fun showAddDrinkBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_add_drink, null)
        dialog.setContentView(view)

        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        val btnQuick250 = view.findViewById<Button>(R.id.btnQuick250)
        val btnQuick500 = view.findViewById<Button>(R.id.btnQuick500)
        val btnQuickGlass = view.findViewById<Button>(R.id.btnQuickGlass)

        val btnMinus50 = view.findViewById<Button>(R.id.btnMinus50)
        val btnPlus50 = view.findViewById<Button>(R.id.btnPlus50)

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // default awal
        var selected = 250
        tvAmount.text = selected.toString()

        fun updateAmount() {
            if (selected < 0) selected = 0
            tvAmount.text = selected.toString()
        }

        btnQuick250.setOnClickListener { selected = 250; updateAmount() }
        btnQuick500.setOnClickListener { selected = 500; updateAmount() }
        btnQuickGlass.setOnClickListener { selected = 250; updateAmount() }

        btnMinus50.setOnClickListener { selected -= 50; updateAmount() }
        btnPlus50.setOnClickListener { selected += 50; updateAmount() }

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            if (selected <= 0) {
                Toast.makeText(this, "Jumlah minum belum dipilih.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentMl += selected
            if (currentMl > targetMl) currentMl = targetMl

            saveDailyProgress()
            updateUI()

            Toast.makeText(this, "+$selected ml dicatat", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
