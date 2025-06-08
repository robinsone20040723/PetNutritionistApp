package com.example.petnutritionistapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBCS = findViewById<Button>(R.id.btnBCS)
        val btnStart = findViewById<Button>(R.id.btnStart)

        // 「認識 BCS」按鈕
        btnBCS.setOnClickListener {
            val intent = Intent(this, BCSIntroductionActivity::class.java)
            startActivity(intent)
        }

        // ✅ 「開始分析狗狗」按鈕 → 導向 DogInputActivity
        btnStart.setOnClickListener {
            val intent = Intent(this, DogInputActivity::class.java)
            startActivity(intent)
        }
    }
}
