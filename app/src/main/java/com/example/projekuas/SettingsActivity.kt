package com.example.projekuas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projekuas.databinding.ActivitySettingsBinding
import com.example.projekuas.notifications.NotificationHelper
import com.example.projekuas.notifications.ReminderScheduler

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val prefs by lazy {
        getSharedPreferences("hydr_prefs", MODE_PRIVATE)
    }

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Izin notifikasi ditolak. Pengingat tidak akan muncul.", Toast.LENGTH_LONG).show()
            binding.switchReminder.isChecked = false
            saveReminderEnabled(false)
            ReminderScheduler.stop(this)
            applyReminderUI(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FooterManager.setupFooter(this)

        // ====== Spinner interval (jam) ======
        val hourOptions = listOf(1, 2, 3, 4) // default
        val hourAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            hourOptions.map { "$it jam" }
        )
        binding.spinnerFrequency.adapter = hourAdapter

        // ====== Spinner ukuran gelas ======
        val glassOptions = listOf(150, 200, 250, 300, 500) // contoh opsi
        val glassAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            glassOptions.map { "$it ml" }
        )
        binding.spinnerGlassSize.adapter = glassAdapter

        // ====== Load state ======
        val enabled = prefs.getBoolean("reminder_enabled", false)
        val intervalHours = prefs.getInt("reminder_interval_hours", 2)
        val glassSize = prefs.getInt("glass_size_ml", 250)

        binding.switchReminder.isChecked = enabled

        // set selection interval
        val hourIdx = hourOptions.indexOf(intervalHours).let { if (it >= 0) it else 0 }
        binding.spinnerFrequency.setSelection(hourIdx)

        // set selection gelas
        val glassIdx = glassOptions.indexOf(glassSize).let { if (it >= 0) it else 0 }
        binding.spinnerGlassSize.setSelection(glassIdx)

        applyReminderUI(enabled)

        // ====== Switch toggle ======
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!ensureNotificationPermission()) {
                    // jangan lanjut dulu kalau permission belum granted
                    binding.switchReminder.isChecked = false
                    return@setOnCheckedChangeListener
                }

                val selectedHours = hourOptions[binding.spinnerFrequency.selectedItemPosition]
                saveReminderEnabled(true)
                saveInterval(selectedHours)

                ReminderScheduler.start(this, selectedHours.toLong())
                Toast.makeText(this, "Pengingat aktif ($selectedHours jam)", Toast.LENGTH_SHORT).show()
                applyReminderUI(true)
            } else {
                saveReminderEnabled(false)
                ReminderScheduler.stop(this)
                Toast.makeText(this, "Pengingat dimatikan", Toast.LENGTH_SHORT).show()
                applyReminderUI(false)
            }
        }

        // ====== Interval diganti saat ON -> restart scheduler ======
        binding.spinnerFrequency.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedHours = hourOptions[position]
                    saveInterval(selectedHours)

                    if (binding.switchReminder.isChecked) {
                        ReminderScheduler.start(this@SettingsActivity, selectedHours.toLong())
                        Toast.makeText(this@SettingsActivity, "Interval: $selectedHours jam", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        // ====== Gelas diganti -> simpan ======
        binding.spinnerGlassSize.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedMl = glassOptions[position]
                    saveGlassSize(selectedMl)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        // ====== Tes notifikasi ======
        binding.btnTestNotif.setOnClickListener {
            if (!ensureNotificationPermission()) return@setOnClickListener
            NotificationHelper.show(this, "Hydrology", "Tes notifikasi berhasil ✅")
        }
    }

    private fun applyReminderUI(enabled: Boolean) {
        // hide/show bagian frekuensi
        binding.layoutFrequency.visibility = if (enabled) View.VISIBLE else View.GONE
        binding.tvReminderStatus.text = if (enabled) "Notifikasi aktif" else "Notifikasi mati"
    }

    private fun ensureNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return false
        }
        return true
    }

    private fun saveReminderEnabled(v: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", v).apply()
    }

    private fun saveInterval(hours: Int) {
        prefs.edit().putInt("reminder_interval_hours", hours).apply()
    }

    private fun saveGlassSize(ml: Int) {
        prefs.edit().putInt("glass_size_ml", ml).apply()
    }
}
