package com.example.projekuas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityOnboarding3Binding
import kotlin.math.round

class Onboarding3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("user_profile", MODE_PRIVATE)
        val weight = prefs.getFloat("weight", -1f) // dari step 2

        if (weight <= 0f) {
            Toast.makeText(this, "Data berat badan belum ada. Kembali ke Step 2 dulu ya.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Preview BMI realtime saat user mengetik
        binding.etHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val heightText = s?.toString()?.trim().orEmpty()
                val heightCm = heightText.toFloatOrNull()

                if (heightCm == null || heightCm <= 0f) {
                    binding.tvBmiPreview.text = "BMI: -"
                    return
                }

                val bmi = calculateBmi(weight, heightCm)
                binding.tvBmiPreview.text = "BMI: ${format2(bmi)}"
            }
        })

        binding.btnNext.setOnClickListener {
            val heightText = binding.etHeight.text.toString().trim()
            if (heightText.isEmpty()) {
                Toast.makeText(this, "Masukkan tinggi badan Anda!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightCm = heightText.toFloat()

            if (heightCm < 80f || heightCm > 250f) {
                Toast.makeText(this, "Tinggi badan tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bmi = calculateBmi(weight, heightCm)

            // Simpan tinggi & bmi
            prefs.edit()
                .putFloat("height_cm", heightCm)
                .putFloat("bmi", bmi)
                .apply()

            // TODO: lanjut ke Step 4
            startActivity(Intent(this, Onboarding4Activity::class.java))
        }

        binding.tvBack.setOnClickListener {
            finish() // balik ke step 2
        }
    }

    private fun calculateBmi(weightKg: Float, heightCm: Float): Float {
        val hM = heightCm / 100f
        return weightKg / (hM * hM)
    }

    private fun format2(value: Float): String {
        return ((round(value * 100)) / 100).toString()
    }
}
