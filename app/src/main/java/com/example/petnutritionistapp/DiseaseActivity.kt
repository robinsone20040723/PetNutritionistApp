package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class DiseaseActivity : AppCompatActivity() {

    private lateinit var tvDiseaseTitle: TextView
    private lateinit var tvPrevention: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease)

        tvDiseaseTitle = findViewById(R.id.tvDiseaseTitle)
        tvPrevention = findViewById(R.id.tvPrevention)
        db = FirebaseFirestore.getInstance()

        val selectedBreed = intent.getStringExtra("DOG_BREED")

        if (selectedBreed != null) {
            db.collection("commonDiseases")
                .document(selectedBreed)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val disease = document.getString("diseases") ?: "無資料"
                        val prevention = document.getString("prevention") ?: "無資料"
                        tvDiseaseTitle.text = "疾病：$disease"
                        tvPrevention.text = "預防建議：$prevention"
                    } else {
                        Toast.makeText(this, "找不到該品種疾病資料", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "讀取資料失敗：${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "未提供品種資料", Toast.LENGTH_SHORT).show()
        }
    }
}
