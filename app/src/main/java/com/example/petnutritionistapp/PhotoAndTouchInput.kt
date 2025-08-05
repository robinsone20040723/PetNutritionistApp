package com.example.petnutritionistapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class PhotoAndTouchInputActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var spinnerRibs: Spinner
    private lateinit var spinnerWaist: Spinner
    private lateinit var spinnerStomach: Spinner
    private lateinit var btnAnalyze: Button

    private val REQUEST_IMAGE_CAPTURE = 1

    private val ribsScoreMap = mapOf(
        "明顯可摸到肋骨" to 2,
        "稍微可摸到" to 4,
        "有脂肪難以摸到" to 7
    )

    private val waistScoreMap = mapOf(
        "腰身明顯" to 3,
        "有點腰身" to 5,
        "無腰身" to 8
    )

    private val stomachScoreMap = mapOf(
        "腹部上提" to 2,
        "腹部平坦" to 5,
        "腹部下垂" to 8
    )

    private fun adjustBCS(imageScore: Int, ribsScore: Int, waistScore: Int, stomachScore: Int): Int {
        val avgTouchScore = (ribsScore + waistScore + stomachScore) / 3.0

        return when {
            imageScore <= 3 && avgTouchScore <= 3 -> 1
            imageScore <= 4 && avgTouchScore <= 4 -> 2
            imageScore <= 5 && avgTouchScore <= 5 -> 3
            imageScore == 5 && avgTouchScore in 4.5..5.5 -> 5
            imageScore >= 8 && avgTouchScore >= 7 -> 9
            else -> ((imageScore * 2 + ribsScore + waistScore + stomachScore) / 5.0)
                .roundToInt()
                .coerceIn(1, 9)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_and_touch_input)

        imgPreview = findViewById(R.id.imgPreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        spinnerRibs = findViewById(R.id.spinnerRibs)
        spinnerWaist = findViewById(R.id.spinnerWaist)
        spinnerStomach = findViewById(R.id.spinnerStomach)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        ArrayAdapter.createFromResource(this, R.array.ribs_array, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRibs.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.waist_array, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWaist.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.stomach_array, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStomach.adapter = it
        }

        btnTakePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        btnAnalyze.setOnClickListener {
            val ribChoice = spinnerRibs.selectedItem.toString()
            val waistChoice = spinnerWaist.selectedItem.toString()
            val stomachChoice = spinnerStomach.selectedItem.toString()

            val ribsScore = ribsScoreMap[ribChoice] ?: 5
            val waistScore = waistScoreMap[waistChoice] ?: 5
            val stomachScore = stomachScoreMap[stomachChoice] ?: 5

            val imageScore = 5 // 目前固定，日後接模型替換

            val finalBCS = adjustBCS(imageScore, ribsScore, waistScore, stomachScore)

            Toast.makeText(this, "預測 BCS 分數為：$finalBCS", Toast.LENGTH_LONG).show()

            val breed = intent.getStringExtra("DOG_BREED") ?: ""

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("TARGET_FRAGMENT", "BCS_RESULT")
            intent.putExtra("FINAL_BCS_SCORE", finalBCS)
            intent.putExtra("DOG_BREED", breed)
            startActivity(intent)
            finish()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imgPreview.setImageBitmap(imageBitmap)
        }
    }
}
