package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MealPlanActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvMealSuggestion: TextView  // 顯示建議的 TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plan)

        // 初始化 Firebase
        db = FirebaseFirestore.getInstance()

        // 綁定 UI 元件
        tvMealSuggestion = findViewById(R.id.tvMealSuggestion)

        // 接收品種與 BCS
        val breed = intent.getStringExtra("DOG_BREED") ?: ""
        val bcsIndex = intent.getIntExtra("BCS_INDEX", -1) + 1  // +1 轉換成 1~9

        if (breed.isNotEmpty() && bcsIndex in 1..9) {
            val docId = "${breed}_$bcsIndex"

            db.collection("mealplans").document(docId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val suggestion = document.getString("suggestion")
                        tvMealSuggestion.text = suggestion ?: "沒有建議內容。"
                    } else {
                        tvMealSuggestion.text = "找不到對應的配餐建議文件。"
                    }
                }
                .addOnFailureListener { e ->
                    tvMealSuggestion.text = "讀取資料失敗：${e.message}"
                    Log.e("MealPlanActivity", "Firebase 錯誤", e)
                }
        } else {
            tvMealSuggestion.text = "未提供有效的狗狗品種或 BCS。"
        }
    }
}
