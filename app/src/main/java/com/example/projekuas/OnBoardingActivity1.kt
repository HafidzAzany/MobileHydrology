package com.example.projekuas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityOnborading1Binding

class Onboarding1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnborading1Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnborading1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val ageText = binding.etAge.text.toString().trim()

            if (ageText.isEmpty()) {
                Toast.makeText(this, "Masukkan usia Anda!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toInt()

            // Simpan ke SharedPreferences
            val prefs = getSharedPreferences("user_profile", MODE_PRIVATE)
            prefs.edit().putInt("age", age).apply()

//            // TODO: Pindah ke Onboarding Step 2
            startActivity(Intent(this, Onboarding2Activity::class.java))
        }

        binding.tvSkip.setOnClickListener {
            // Langsung dashboard
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Langsung dashboard
        binding.tvSkip.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
