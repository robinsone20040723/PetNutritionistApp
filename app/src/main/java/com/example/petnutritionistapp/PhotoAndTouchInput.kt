package com.example.petnutritionistapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.petnutritionistapp.ai.BCSClassifier

class PhotoAndTouchInputActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnPickPhoto: Button
    private lateinit var btnAnalyze: Button

    private var capturedBitmap: Bitmap? = null
    private var dogBreed: String? = null
    private var bcsClassifier: BCSClassifier? = null

    /* =========================
       📸 拍照
       ========================= */
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
                btnAnalyze.isEnabled = true
            } else {
                toast("拍照失敗")
            }
        }

    /* =========================
       🖼️ 相簿
       ========================= */
    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = uriToBitmap(it)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    imgPreview.setImageBitmap(bitmap)
                    btnAnalyze.isEnabled = true
                } else {
                    toast("讀取圖片失敗")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_and_touch_input)

        dogBreed = intent.getStringExtra("DOG_BREED")

        imgPreview = findViewById(R.id.imgPreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnPickPhoto = findViewById(R.id.btnPickPhoto)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        btnAnalyze.isEnabled = false

        btnTakePhoto.setOnClickListener { checkCameraPermissionAndOpen() }
        btnPickPhoto.setOnClickListener { openGallery() }
        btnAnalyze.setOnClickListener { analyzeBCS() }
    }

    /* =========================
       🤖 AI BCS 分析（✅最終正確版）
       ========================= */
    private fun analyzeBCS() {

        val rawBitmap = capturedBitmap ?: run {
            toast("尚未選擇照片")
            return
        }

        // ⭐⭐⭐ 關鍵：強制轉成「可讀像素 Bitmap」
        val safeBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, false)

        if (bcsClassifier == null) {
            bcsClassifier = BCSClassifier(this)
        }

        try {
            val bcsScore = bcsClassifier!!.classify(safeBitmap)
            Log.e("BCS_RESULT", "BCS = $bcsScore")
            goToResult(bcsScore)

        } catch (t: Throwable) {
            t.printStackTrace()
            Log.e("BCS_RESULT", "AI 分析失敗", t)
            toast("AI 分析失敗：${t.javaClass.simpleName}")
        }
    }



    /* =========================
       ➡️ 結果頁
       ========================= */
    private fun goToResult(score: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("TARGET_FRAGMENT", "BCS_RESULT")
            putExtra("FINAL_BCS_SCORE", score)
            putExtra("DOG_BREED", dogBreed ?: "")
        }
        startActivity(intent)
        finish()
    }

    /* =========================
       工具
       ========================= */
    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        } else {
            takePictureLauncher.launch(null)
        }
    }

    private fun openGallery() {
        pickPhotoLauncher.launch("image/*")
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
