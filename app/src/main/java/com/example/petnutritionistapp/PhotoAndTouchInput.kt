package com.example.petnutritionistapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PhotoAndTouchInputActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var spinnerRibs: Spinner
    private lateinit var spinnerWaist: Spinner
    private lateinit var spinnerStomach: Spinner
    private lateinit var btnAnalyze: Button

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_and_touch_input)

        imgPreview = findViewById(R.id.imgPreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        spinnerRibs = findViewById(R.id.spinnerRibs)
        spinnerWaist = findViewById(R.id.spinnerWaist)
        spinnerStomach = findViewById(R.id.spinnerStomach)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        // 設定選單選項
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

            Toast.makeText(this, "肋骨:$ribChoice\n腰部:$waistChoice\n腹部:$stomachChoice", Toast.LENGTH_LONG).show()

            // TODO：這裡你可以加上自訂規則計算出 BCS 分數，並跳轉下一頁
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
