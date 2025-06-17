package com.example.petnutritionistapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.app.AlertDialog
import android.widget.ProgressBar

class DogInputActivity : AppCompatActivity() {

    private lateinit var spinnerSize: Spinner
    private lateinit var spinnerBreed: Spinner
    private lateinit var spinnerBCS: Spinner
    private lateinit var db: FirebaseFirestore

    private val sizeMap = mapOf(
        "迷你犬" to listOf("吉娃娃", "博美", "馬爾濟斯", "約克夏"),
        "小型犬" to listOf("雪納瑞", "米克斯", "貴賓犬", "臘腸犬", "法國鬥牛犬", "比熊犬", "西施犬", "狐狸犬", "巴哥犬", "西高地白犬"),
        "中型犬" to listOf("柴犬", "邊境牧羊犬", "可卡犬"),
        "大型犬" to listOf("黃金獵犬", "哈士奇", "拉布拉多", "沙皮犬", "杜賓犬", "大丹犬")
    )

    private val bcsList = listOf(
        "1. 肋骨、腰椎骨、骨盆骨突出，看不到脂肪，明顯肌肉流失",
        "2. 可見肋骨、腰椎骨、骨盆骨，摸不到脂肪，極少量肌肉",
        "3. 輕易看到肋骨與骨盆骨，明顯腰身與腹部凹陷",
        "4. 肋骨上有少許脂肪包覆，腰身與腹部凹陷明顯",
        "5. 體態良好，肋骨摸起來略有脂肪，腰身明顯",
        "6. 肋骨有脂肪覆蓋，腰身不明顯，腹部凹陷明顯",
        "7. 脂肪覆蓋肋骨、尾根部堆積，腰身消失（直筒腰）",
        "8. 脂肪非常厚，腰身消失，腹圍膨大",
        "9. 大量脂肪覆蓋胸腔與脊椎，腹圍大、無腰身，頸部與四肢也堆積"
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_dog_input)

        db = FirebaseFirestore.getInstance()

        spinnerSize = findViewById(R.id.spinnerSize)
        spinnerBreed = findViewById(R.id.spinnerBreed)
        spinnerBCS = findViewById(R.id.spinnerBCS)

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

        spinnerBCS.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bcsList)

        val btnNext = findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            val size = spinnerSize.selectedItem?.toString() ?: ""
            val breed = spinnerBreed.selectedItem?.toString() ?: ""
            val bcs = spinnerBCS.selectedItem?.toString() ?: ""

            if (size.isEmpty() || breed.isEmpty() || bcs.isEmpty()) {
                Toast.makeText(this, "請選擇完整資訊後再繼續", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dogData = hashMapOf(
                "size" to size,
                "breed" to breed,
                "bcs" to bcs
            )

            Log.d("DogInput", "準備上傳: $dogData")

            // 顯示 loading 對話框
            val loadingDialog = showLoadingDialog()

            db.collection("dogInputs")
                .add(dogData)
                .addOnSuccessListener {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "✅ 資料已上傳", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, BCSResultActivity::class.java)
                    intent.putExtra("BCS_INDEX", spinnerBCS.selectedItemPosition)
                    intent.putExtra("BREED_NAME", breed)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    loadingDialog.dismiss()
                    Toast.makeText(this, "❌ 上傳失敗：${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("DogInput", "Firestore 上傳錯誤", e)
                }
        }
    }

    // 🔄 Loading Dialog 方法
    private fun showLoadingDialog(): AlertDialog {
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }
        return AlertDialog.Builder(this)
            .setTitle("請稍候")
            .setView(progressBar)
            .setCancelable(false)
            .show()
    }
}
