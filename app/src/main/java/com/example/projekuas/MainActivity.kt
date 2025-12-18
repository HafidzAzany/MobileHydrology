package com.example.projekuas

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var env: OrtEnvironment
    private lateinit var session: OrtSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup dropdown
        setupSpinners()

        // Load ONNX model
        try {
            loadModel()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Tombol Prediksi
        findViewById<Button>(R.id.btnPredict).setOnClickListener {
            try {
                doPredict()
            } catch (e: Exception) {
                Toast.makeText(this, "Mohon isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // VALIDASI INPUT – supaya tidak masuk angka tidak masuk akal

    private fun validateInput(
        age: Float,
        weight: Float,
        height: Float,
        temp: Float,
        activity: Float
    ): String? {

        if (age !in 1f..100f)
            return "Usia harus antara 1–100 tahun."

        if (weight !in 5f..200f)
            return "Berat badan harus antara 5–200 kg."

        if (height !in 50f..220f)
            return "Tinggi badan harus antara 50–220 cm."

        if (temp !in 10f..45f)
            return "Suhu lingkungan tidak realistis. Masukkan 10–45°C."

        if (activity !in 0f..12f)
            return "Durasi aktivitas harus 0–12 jam."

        return null
    }

    // SETUP SPINNER
    private fun setupSpinners() {

        val healthOptions = arrayOf("Sehat", "Sedang Sakit/Demam")
        findViewById<Spinner>(R.id.spHealth).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, healthOptions)

        val heatOptions = arrayOf("Cuaca Normal", "Sangat Panas/Terik")
        findViewById<Spinner>(R.id.spHeatStress).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, heatOptions)

        val activityOptions = arrayOf(
            "Ringan (Duduk)",
            "Sedang (Jalan/Lari Kecil)",
            "Berat (Angkat Beban/Buruh)"
        )
        findViewById<Spinner>(R.id.spActivityFactor).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activityOptions)

        val riskOptions = arrayOf("Jarang Haus (Rendah)", "Mudah Haus (Tinggi)")
        findViewById<Spinner>(R.id.spHydrationRisk).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, riskOptions)
    }

    // LOAD MODEL ONNX
    private fun loadModel() {
        env = OrtEnvironment.getEnvironment()
        val bytes = assets.open("hydrology_model.onnx").readBytes()
        session = env.createSession(bytes)
    }

    // EKSEKUSI PREDIKSI
    private fun doPredict() {

        // --- Ambil input user ---
        val age = findViewById<EditText>(R.id.etAge).text.toString().toFloat()
        val weight = findViewById<EditText>(R.id.etWeight).text.toString().toFloat()
        val heightCm = findViewById<EditText>(R.id.etHeight).text.toString().toFloat()
        val temp = findViewById<EditText>(R.id.etTemp).text.toString().toFloat()
        val activityDuration = findViewById<EditText>(R.id.etActivity).text.toString().toFloat()

        // --- VALIDASI ---
        val valid = validateInput(age, weight, heightCm, temp, activityDuration)
        if (valid != null) {
            Toast.makeText(this, valid, Toast.LENGTH_LONG).show()
            return
        }

        // --- Hitung BMI ---
        val heightM = heightCm / 100f
        val bmi = weight / (heightM * heightM)

        // --- Ambil nilai Spinner ---
        val healthValue = findViewById<Spinner>(R.id.spHealth).selectedItemPosition.toFloat()
        val heatStressValue = findViewById<Spinner>(R.id.spHeatStress).selectedItemPosition.toFloat()
        val activityFactorValue = findViewById<Spinner>(R.id.spActivityFactor).selectedItemPosition.toFloat()
        val hydrationRiskValue = findViewById<Spinner>(R.id.spHydrationRisk).selectedItemPosition.toFloat()

        // --- Siapkan input model (urutan penting!) ---
        val inputs = floatArrayOf(
            age, weight, temp, activityDuration,
            healthValue, bmi, heatStressValue,
            activityFactorValue, hydrationRiskValue
        )

        // --- Jalankan model ONNX ---
        val shape = longArrayOf(1, 9)
        val tensor = OnnxTensor.createTensor(env, java.nio.FloatBuffer.wrap(inputs), shape)
        val outputs = session.run(mapOf("input" to tensor))

        // --- Ambil hasil ---
        val output = outputs[0].value as Array<FloatArray>
        val prediction = output[0][0]

        // --- Tampilkan hasil ---
        val tvResult = findViewById<TextView>(R.id.tvResult)
        tvResult.text = "Hasil: Tubuh Anda membutuhkan sekitar %.2f Liter air hari ini."
            .format(prediction)
    }



}
