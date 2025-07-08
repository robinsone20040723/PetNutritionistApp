package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent


class BCSResultActivity : AppCompatActivity() {

    private lateinit var ivDogImage: ImageView
    private lateinit var tvResult: TextView
    private lateinit var tvSuggestion: TextView
    private lateinit var btnMeal: Button
    private lateinit var btnDisease: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bcs_result)

        // 初始化元件
        ivDogImage = findViewById(R.id.ivDogImage)
        tvResult = findViewById(R.id.tvResult)
        tvSuggestion = findViewById(R.id.tvSuggestion)
        btnMeal = findViewById(R.id.btnMeal)
        btnDisease = findViewById(R.id.btnDisease)
        db = FirebaseFirestore.getInstance()

        val bcsIndex = intent.getIntExtra("BCS_INDEX", -1)
        val breedName = intent.getStringExtra("BREED_NAME") ?: ""

        // 顯示 BCS 結果
        val resultText: String
        val suggestionText: String
        val selectedBreed = intent.getStringExtra("DOG_BREED") ?: "未知品種"


        when (bcsIndex) {
            0, 1 -> {
                resultText = "您的狗狗為：超瘦（BCS 1-2 分）"
                suggestionText = "建議補充足夠熱量與蛋白質，並持續觀察體態變化。"
            }
            2, 3 -> {
                resultText = "您的狗狗為：過瘦（BCS 3-4 分）"
                suggestionText = "建議增加飲食量與營養密度，提升體態。"
            }
            4 -> {
                resultText = "您的狗狗為：適中（BCS 5 分）"
                suggestionText = "目前體態良好，請持續維持良好飲食與運動。"
            }
            5, 6 -> {
                resultText = "您的狗狗為：過重（BCS 6-7 分）"
                suggestionText = "建議減少熱量攝取並增加運動時間。"
            }
            7, 8 -> {
                resultText = "您的狗狗為：超重（BCS 8-9 分）"
                suggestionText = "請積極進行體重控制，避免引發相關疾病。"
            }
            else -> {
                resultText = "無法判斷狗狗體態"
                suggestionText = "請重新進行體態分析。"
            }
        }

        tvResult.text = resultText
        tvSuggestion.text = suggestionText

        // 讀取圖片
        if (breedName.isNotEmpty()) {
            db.collection("breedImages")
                .document(breedName)
                .get()
                .addOnSuccessListener { doc ->
                    val imageUrl = doc.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(ivDogImage)
                    } else {
                        Toast.makeText(this, "找不到圖片連結", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "讀取圖片失敗", Toast.LENGTH_SHORT).show()
                }
        }

        btnMeal.setOnClickListener {
            val intent = Intent(this, MealPlanActivity::class.java)
            intent.putExtra("DOG_BREED", selectedBreed) // ✅ 現在這行會正常
            intent.putExtra("BCS_INDEX", bcsIndex)
            startActivity(intent)
        }



        btnDisease.setOnClickListener {
            val intent = Intent(this, DiseaseActivity::class.java)
            intent.putExtra("DOG_BREED", selectedBreed) // 傳入狗的品種
            startActivity(intent)
        }

    }
}

