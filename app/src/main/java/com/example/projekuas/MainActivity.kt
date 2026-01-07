package com.example.projekuas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projekuas.data.AppDatabase
import com.example.projekuas.data.DailyProgressEntity
import com.example.projekuas.data.DrinkLogEntity
import com.example.projekuas.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Room
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var db: AppDatabase

    // progress minum harian (state di memory)
    private var targetMl = 2000
    private var currentMl = 0

    // realtime date
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FooterManager.setupFooter(this)

        //  navbar/footer (punya teman)
        FooterManager.setupFooter(this)

        //  Room init
        db = AppDatabase.getInstance(this)

        //  Tanggal real-time
        startRealTimeDate()

        //  Load progress dari Room untuk hari ini
        loadDailyProgressFromRoom()

        binding.btnAddDrink.setOnClickListener {
            showAddDrinkBottomSheet()
        }
    }

    private fun startRealTimeDate() {
        dateRunnable = object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale("id", "ID"))
                binding.tvDate.text = sdf.format(Date())
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(dateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dateRunnable)
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun loadDailyProgressFromRoom() {
        val key = todayKey()

        ioScope.launch {
            val dao = db.dailyProgressDao()
            val data = dao.getByDate(key)

            if (data == null) {
                val newData = DailyProgressEntity(
                    dateKey = key,
                    targetMl = 2000,
                    currentMl = 0
                )
                dao.upsert(newData)
                targetMl = newData.targetMl
                currentMl = newData.currentMl
            } else {
                targetMl = data.targetMl
                currentMl = data.currentMl
            }

            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun saveDailyProgressToRoom() {
        val key = todayKey()
        ioScope.launch {
            db.dailyProgressDao().upsert(
                DailyProgressEntity(
                    dateKey = key,
                    targetMl = targetMl,
                    currentMl = currentMl
                )
            )
        }
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
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
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

            // update memory + UI
            currentMl += selected
            if (currentMl > targetMl) currentMl = targetMl
            updateUI()

            // simpan progress harian
            saveDailyProgressToRoom()

            // simpan log riwayat
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    db.drinkLogDao().insert(
                        DrinkLogEntity(
                            dateKey = todayKey(),
                            timeMillis = System.currentTimeMillis(),
                            amountMl = selected,
                            source = "manual",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(this, "+$selected ml dicatat", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
