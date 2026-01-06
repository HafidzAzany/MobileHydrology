package com.example.projekuas

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PredictionActivity : AppCompatActivity() {

    private lateinit var env: OrtEnvironment
    private lateinit var session: OrtSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        setupSpinners()

        try {
            loadModel()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btnPredict).setOnClickListener {
            try {
                doPredict()
            } catch (e: Exception) {
                Toast.makeText(this, "Mohon isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun validateInput(
        age: Float,
        weight: Float,
        height: Float,
        temp: Float,
        activity: Float
    ): String? {
        if (age !in 1f..100f) return "Usia harus antara 1–100 tahun."
        if (weight !in 5f..200f) return "Berat badan harus antara 5–200 kg."
        if (height !in 50f..220f) return "Tinggi badan harus antara 50–220 cm."
        if (temp !in 10f..45f) return "Suhu lingkungan harus 10–45°C."
        return null
    }

    private fun setupSpinners() {
        fun spinnerAdapterSelectedWhiteDropdownBlack(items: Array<String>): ArrayAdapter<String> {
            return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val tv = view.findViewById<TextView>(android.R.id.text1)
                    tv.setTextColor(Color.WHITE)
                    tv.textSize = 14f
                    return view
                }
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

        findViewById<Spinner>(R.id.spHealth).adapter = spinnerAdapterSelectedWhiteDropdownBlack(arrayOf("Sehat", "Sedang Sakit/Demam"))
        findViewById<Spinner>(R.id.spHeatStress).adapter = spinnerAdapterSelectedWhiteDropdownBlack(arrayOf("Cuaca Normal", "Sangat Panas/Terik"))
        findViewById<Spinner>(R.id.spActivityFactor).adapter = spinnerAdapterSelectedWhiteDropdownBlack(arrayOf("Ringan (Duduk)", "Sedang (Jalan/Lari Kecil)", "Berat (Angkat Beban/Buruh)"))
        findViewById<Spinner>(R.id.spHydrationRisk).adapter = spinnerAdapterSelectedWhiteDropdownBlack(arrayOf("Jarang Haus (Rendah)", "Mudah Haus (Tinggi)"))
    }

    private fun loadModel() {
        env = OrtEnvironment.getEnvironment()
        val modelBytes = assets.open("hydrology_model.onnx").readBytes()
        session = env.createSession(modelBytes)
    }

    private fun doPredict() {
        val ageStr = findViewById<EditText>(R.id.etAge).text.toString()
        val weightStr = findViewById<EditText>(R.id.etWeight).text.toString()
        val heightStr = findViewById<EditText>(R.id.etHeight).text.toString()
        val tempStr = findViewById<EditText>(R.id.etTemp).text.toString()
        val activityStr = findViewById<EditText>(R.id.etActivity).text.toString()

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || tempStr.isEmpty() || activityStr.isEmpty()) {
            Toast.makeText(this, "Harap isi semua bidang!", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toFloat()
        val weight = weightStr.toFloat()
        val heightCm = heightStr.toFloat()
        val temp = tempStr.toFloat()
        val activityDuration = activityStr.toFloat()

        val error = validateInput(age, weight, heightCm, temp, activityDuration)
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }

        val heightM = heightCm / 100f
        val bmi = weight / (heightM * heightM)

        val health = findViewById<Spinner>(R.id.spHealth).selectedItemPosition.toFloat()
        val heat = findViewById<Spinner>(R.id.spHeatStress).selectedItemPosition.toFloat()
        val activityFactor = findViewById<Spinner>(R.id.spActivityFactor).selectedItemPosition.toFloat()
        val risk = findViewById<Spinner>(R.id.spHydrationRisk).selectedItemPosition.toFloat()

        val inputs = floatArrayOf(age, weight, temp, activityDuration, health, bmi, heat, activityFactor, risk)

        val tensor = OnnxTensor.createTensor(env, java.nio.FloatBuffer.wrap(inputs), longArrayOf(1, 9))
        val output = session.run(mapOf("input" to tensor))[0].value as Array<FloatArray>
        val prediction = output[0][0]

        showResultDialog(prediction)
    }

    private fun showResultDialog(prediction: Float) {
        val resultText = "Tubuh Anda membutuhkan sekitar %.2f Liter air hari ini.".format(prediction)
        
        // Simpan ke SharedPreferences agar Dashboard terupdate
        val targetMl = (prediction * 1000).toInt()
        getSharedPreferences("daily_progress", MODE_PRIVATE)
            .edit()
            .putInt("target_ml", targetMl)
            .apply()

        AlertDialog.Builder(this)
            .setTitle("Hasil Prediksi")
            .setMessage("$resultText\n\nApakah Anda ingin lanjut ke Dashboard?")
            .setPositiveButton("Ya, Lanjut") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Hitung Ulang", null)
            .show()
    }
}
