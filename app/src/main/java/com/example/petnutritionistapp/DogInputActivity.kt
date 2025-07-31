package com.example.petnutritionistapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class DogInputActivity : AppCompatActivity() {

    private lateinit var spinnerSize: Spinner
    private lateinit var spinnerBreed: Spinner

    private val sizeMap = mapOf(
        "迷你犬" to listOf("吉娃娃", "博美", "馬爾濟斯", "約克夏"),
        "小型犬" to listOf("雪納瑞", "米克斯", "貴賓犬", "臘腸犬", "法國鬥牛犬", "比熊犬", "西施犬", "狐狸犬", "巴哥犬", "西高地白犬"),
        "中型犬" to listOf("柴犬", "邊境牧羊犬", "可卡犬"),
        "大型犬" to listOf("黃金獵犬", "哈士奇", "拉布拉多", "沙皮犬", "杜賓犬", "大丹犬")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_dog_input)

        spinnerSize = findViewById(R.id.spinnerSize)
        spinnerBreed = findViewById(R.id.spinnerBreed)

        val sizeList = sizeMap.keys.toList()
        spinnerSize.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizeList)

        spinnerSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSize = sizeList[position]
                val breeds = sizeMap[selectedSize] ?: listOf()
                spinnerBreed.adapter = ArrayAdapter(this@DogInputActivity, android.R.layout.simple_spinner_dropdown_item, breeds)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 按鈕 ID 請確保是 activity_dog_input.xml 裡的 btnNext
        val btnStartPhoto = findViewById<Button>(R.id.btnNext)
        btnStartPhoto.setOnClickListener {
            val size = spinnerSize.selectedItem?.toString() ?: ""
            val breed = spinnerBreed.selectedItem?.toString() ?: ""

            if (size.isEmpty() || breed.isEmpty()) {
                Toast.makeText(this, "請選擇體型與品種", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 跳轉到拍照與觸感選單頁面
            val intent = Intent(this, PhotoAndTouchInputActivity::class.java)
            intent.putExtra("DOG_SIZE", size)
            intent.putExtra("DOG_BREED", breed)
            startActivity(intent)
        }
    }
}
