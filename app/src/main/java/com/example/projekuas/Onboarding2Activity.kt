package com.example.projekuas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityOnboarding2Binding

class Onboarding2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val weightText = binding.etWeight.text.toString().trim()

            if (weightText.isEmpty()) {
                Toast.makeText(this, "Masukkan berat badan Anda!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toFloat()

            if (weight <= 0f || weight > 300f) {
                Toast.makeText(this, "Berat badan tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan ke SharedPreferences
            val prefs = getSharedPreferences("user_profile", MODE_PRIVATE)
            prefs.edit().putFloat("weight", weight).apply()

//            // TODO: lanjut ke Onboarding Step 3 (tinggi badan)
            startActivity(Intent(this, Onboarding3Activity::class.java))
        }

        binding.tvBack.setOnClickListener {
            finish() // balik ke step 1
        }
    }
}
