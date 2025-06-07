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

        btnBCS.setOnClickListener {
            val intent = Intent(this, BCSIntroductionActivity::class.java)
            startActivity(intent)
        }


        btnStart.setOnClickListener {
            // TODO: 開啟狗狗分析頁面
        }
    }
}
