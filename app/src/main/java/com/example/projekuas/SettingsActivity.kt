package com.example.projekuas

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Footer Navigation
        FooterManager.setupFooter(this)

        setupSpinners()
        setupListeners()
    }

    private fun setupListeners() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutFrequency.alpha = if (isChecked) 1.0f else 0.5f
            binding.spinnerFrequency.isEnabled = isChecked
            binding.tvReminderStatus.text = if (isChecked) "Notifikasi aktif" else "Notifikasi nonaktif"
        }
    }

    private fun setupSpinners() {
        val frequencyOptions = arrayOf(
            "Setiap 30 menit", "Setiap 1 jam", "Setiap 1.5 jam",
            "Setiap 2 jam", "Setiap 3 jam", "Setiap 4 jam"
        )
        val frequencyAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, frequencyOptions)
        frequencyAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
        binding.spinnerFrequency.adapter = frequencyAdapter
        binding.spinnerFrequency.setSelection(3)

        val glassOptions = arrayOf(
            "150 ml (Gelas kecil)", "200 ml (Gelas sedang)", "250 ml (Gelas standar)",
            "300 ml (Gelas besar)", "350 ml (Gelas ekstra besar)", "500 ml (Botol)"
        )
        val glassAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, glassOptions)
        glassAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
        binding.spinnerGlassSize.adapter = glassAdapter
        binding.spinnerGlassSize.setSelection(2)
    }
}