package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AIAdvisorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_advisor)

        val input = findViewById<EditText>(R.id.inputQuestion)
        val btnAsk = findViewById<Button>(R.id.btnAsk)
        val result = findViewById<TextView>(R.id.resultText)

        btnAsk.setOnClickListener {
            val question = input.text.toString()
            if (question.isNotBlank()) {
                result.text = "模擬回答：目前無連線 GPT，未來將使用 OpenAI API 回應此問題。\n\n你問的是：$question"
            }
        }
    }
}
