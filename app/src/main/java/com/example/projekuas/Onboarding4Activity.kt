package com.example.projekuas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityOnboarding4Binding

class Onboarding4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()

        binding.btnNext.setOnClickListener { saveStep4() }
        binding.tvBack.setOnClickListener { finish() }
    }

    // Adapter: selected item PUTIH, dropdown item HITAM
    private fun spinnerAdapterSelectedWhiteDropdownBlack(items: Array<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items
        ) {
            // Tampilan yang muncul di "kotak spinner" setelah dipilih
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(Color.WHITE)
                tv.textSize = 14f
                return view
            }

            // Tampilan item di dropdown
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(Color.BLACK)
                tv.textSize = 14f
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupSpinners() {
        // Health: 0 Sehat, 1 Sakit
        val healthOptions = arrayOf("Sehat", "Sedang Sakit/Demam")
        binding.spHealth.adapter = spinnerAdapterSelectedWhiteDropdownBlack(healthOptions)

        // Heat stress: 0 Normal, 1 Panas
        val heatOptions = arrayOf("Cuaca Normal", "Sangat Panas/Terik")
        binding.spHeatStress.adapter = spinnerAdapterSelectedWhiteDropdownBlack(heatOptions)

        // Activity factor: 0 Ringan, 1 Sedang, 2 Berat
        val activityOptions = arrayOf(
            "Ringan (Duduk)",
            "Sedang (Jalan/Lari Kecil)",
            "Berat (Angkat Beban/Buruh)"
        )
        binding.spActivityFactor.adapter = spinnerAdapterSelectedWhiteDropdownBlack(activityOptions)

        // Hydration risk: 0 Rendah, 1 Tinggi
        val riskOptions = arrayOf("Jarang Haus (Rendah)", "Mudah Haus (Tinggi)")
        binding.spHydrationRisk.adapter = spinnerAdapterSelectedWhiteDropdownBlack(riskOptions)
    }

    private fun saveStep4() {
        val tempText = binding.etTemp.text.toString().trim()
        val actText = binding.etActivity.text.toString().trim()

        if (tempText.isEmpty() || actText.isEmpty()) {
            Toast.makeText(this, "Isi suhu dan durasi aktivitas dulu ya!", Toast.LENGTH_SHORT).show()
            return
        }

        val temp = tempText.toFloatOrNull()
        val activityDuration = actText.toFloatOrNull()

        if (temp == null || activityDuration == null) {
            Toast.makeText(this, "Format angka tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi basic
        if (temp < 0 || temp > 60) {
            Toast.makeText(this, "Suhu tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }
        if (activityDuration < 0 || activityDuration > 24) {
            Toast.makeText(this, "Durasi aktivitas tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        val healthValue = binding.spHealth.selectedItemPosition.toFloat()
        val heatStressValue = binding.spHeatStress.selectedItemPosition.toFloat()
        val activityFactorValue = binding.spActivityFactor.selectedItemPosition.toFloat()
        val hydrationRiskValue = binding.spHydrationRisk.selectedItemPosition.toFloat()

        val prefs = getSharedPreferences("user_profile", MODE_PRIVATE)
        prefs.edit()
            .putFloat("temp", temp)
            .putFloat("activity_duration", activityDuration)
            .putFloat("health", healthValue)
            .putFloat("heat_stress", heatStressValue)
            .putFloat("activity_factor", activityFactorValue)
            .putFloat("hydration_risk", hydrationRiskValue)
            .putBoolean("onboarding_done", true)
            .apply()

        Toast.makeText(this, "Profil tersimpan!", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
