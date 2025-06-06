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
            // TODO: 開啟 BCS 介紹畫面
        }

        btnStart.setOnClickListener {
            // TODO: 開啟狗狗分析頁面
        }
    }
}
