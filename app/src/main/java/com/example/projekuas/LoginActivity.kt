package com.example.projekuas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekuas.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol Login
        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        // Tombol Register
        binding.registerNowText.setOnClickListener {
            val intent = Intent(this, Onboarding1Activity::class.java)
            startActivity(intent)
        }
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, Onboarding1Activity::class.java))
        }

    }

    private fun handleLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan kata sandi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (email == "demo@hydrology.com" && password == "demo123") {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PredictionActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Email atau kata sandi salah", Toast.LENGTH_SHORT).show()
        }
    }
}