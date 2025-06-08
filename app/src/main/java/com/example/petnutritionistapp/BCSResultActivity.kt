package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BCSResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bcs_result)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvSuggestion = findViewById<TextView>(R.id.tvSuggestion)
        val btnMeal = findViewById<Button>(R.id.btnMeal)
        val btnDisease = findViewById<Button>(R.id.btnDisease)

        // 假設你是從前一頁傳過來一個 bcsIndex 變數
        val bcsIndex = intent.getIntExtra("BCS_INDEX", -1)

        val resultText = when (bcsIndex) {
            0 -> "您的狗狗為：過瘦"
            1 -> "您的狗狗為：微瘦"
            2 -> "您的狗狗為：適中"
            3 -> "您的狗狗為：微胖"
            4 -> "您的狗狗為：過胖"
            else -> "無法判斷狗狗體態"
        }

        val suggestionText = when (bcsIndex) {
            0 -> "建議多補充營養與增加熱量攝取。"
            1 -> "建議略增加餐量，保持觀察。"
            2 -> "體態理想，請繼續保持！"
            3 -> "建議減少熱量攝取與增加運動。"
            4 -> "體重過重，應積極調整飲食與運動！"
            else -> "請重新分析一次。"
        }

        tvResult.text = resultText
        tvSuggestion.text = suggestionText

        btnMeal.setOnClickListener {
            // TODO: 跳轉到配餐畫面
        }

        btnDisease.setOnClickListener {
            // TODO: 跳轉到常見疾病畫面
        }
    }
}
