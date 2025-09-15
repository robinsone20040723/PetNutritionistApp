package com.example.petnutritionistapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.petnutritionistapp.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storageMetadata
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.round // <-- NEW: for Double.roundToInt()

class PhotoAndTouchInputActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var spinnerRibs: Spinner
    private lateinit var spinnerWaist: Spinner
    private lateinit var spinnerStomach: Spinner
    private lateinit var btnAnalyze: Button

    // Firebase
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }

    private var uploadedImageUrl: String? = null

    // ===== Retrofit =====
    private val BASE_URL = "https://asia-east1-petnutritionist-f1f8d.cloudfunctions.net/api/"
    private val api by lazy { provideApi(BASE_URL) }

    private fun provideApi(baseUrl: String): ApiService {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    // ===== 相機/相簿 =====
    private var cameraOutputUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraOutputUri
        if (success && uri != null) {
            onImageReady(uri)
        } else {
            toast("拍照失敗")
        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            takePersistableReadPermissionIfPossible(it)
            onImageReady(it)
        }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            takePersistableReadPermissionIfPossible(it)
            onImageReady(it)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera() else toast("未授權相機，無法拍照")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_and_touch_input)

        // Firebase 初始化 + （開發期）App Check Debug
        FirebaseApp.initializeApp(this)
        runCatching {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        }

        // ✅ 先確保登入（匿名即可，避免 Storage 規則擋）
        ensureSignedIn()

        imgPreview     = findViewById(R.id.imgPreview)
        btnTakePhoto   = findViewById(R.id.btnTakePhoto)
        spinnerRibs    = findViewById(R.id.spinnerRibs)
        spinnerWaist   = findViewById(R.id.spinnerWaist)
        spinnerStomach = findViewById(R.id.spinnerStomach)
        btnAnalyze     = findViewById(R.id.btnAnalyze)

        // 下拉初始化
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

        // 上傳未完成前鎖定分析鍵
        btnAnalyze.isEnabled = false

        btnTakePhoto.setOnClickListener { showSourceChooser() }

        btnAnalyze.setOnClickListener {
            val url = uploadedImageUrl
            if (url.isNullOrEmpty()) {
                toast("請先拍照或選圖，並等圖片上傳完成")
                return@setOnClickListener
            }
            val ribsText = spinnerRibs.selectedItem?.toString() ?: return@setOnClickListener
            val waistText = spinnerWaist.selectedItem?.toString() ?: return@setOnClickListener
            val stomachText = spinnerStomach.selectedItem?.toString() ?: return@setOnClickListener
            callBcsApi(url, ribsText, waistText, stomachText)
        }
    }

    // === 登入（匿名） ===
    private fun ensureSignedIn() {
        if (auth.currentUser != null) return
        auth.signInAnonymously()
            .addOnSuccessListener { android.util.Log.d("Auth", "Anonymous signed in: ${it.user?.uid}") }
            .addOnFailureListener { e -> toast("匿名登入失敗：${e.message}") }
    }

    // === 來源選擇 ===
    private fun showSourceChooser() {
        MaterialAlertDialogBuilder(this)
            .setTitle("選擇照片來源")
            .setItems(arrayOf("拍照", "從相簿選擇")) { _, which ->
                when (which) {
                    0 -> ensureCameraThenOpen()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun ensureCameraThenOpen() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) openCamera() else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        cameraOutputUri = createImageUri()
        val output = cameraOutputUri
        if (output == null) {
            toast("無法建立相片檔案")
            return
        }
        takePicture.launch(output)
    }

    private fun openGallery() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            getContent.launch("image/*")
        }
    }

    private fun createImageUri(): Uri? = try {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val dir = File(cacheDir, "images").apply { if (!exists()) mkdirs() }
        val file = File(dir, "IMG_$time.jpg").apply { if (exists()) delete() }
        FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    } catch (e: Exception) {
        android.util.Log.e("Camera", "createImageUri failed", e)
        null
    }

    // === 選到/拍到圖片後 ===
    private fun onImageReady(uri: Uri) {
        android.util.Log.d("Upload", "local uri = $uri")

        Glide.with(this).load(uri).centerCrop().into(imgPreview)

        btnAnalyze.isEnabled = false
        uploadedImageUrl = null

        uploadUriToStorage(uri) { url ->
            uploadedImageUrl = url
            btnAnalyze.isEnabled = url != null
            if (url == null) toast("圖片上傳失敗，請重試")
            else toast("圖片已上傳完成")
        }
    }

    private fun takePersistableReadPermissionIfPossible(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Photo Picker 多半沒給 persistable flag，失敗可忽略
        }
    }

    // === 上傳到 Firebase Storage ===
    private fun uploadUriToStorage(uri: Uri, onComplete: (String?) -> Unit) {
        val fileName = "bcs_${System.currentTimeMillis()}.jpg"
        val ref = storageRef.child("bcsUploads/$fileName")
        android.util.Log.d("Upload", "dst = gs://${ref.bucket}/${ref.path}")

        val metadata = storageMetadata { contentType = "image/jpeg" }

        ref.putFile(uri, metadata)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { dl ->
                        android.util.Log.d("Upload", "downloadUrl = $dl")
                        onComplete(dl.toString())
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("Upload", "get downloadUrl failed", e)
                        toast("取得下載連結失敗：${e.message}")
                        onComplete(null)
                    }
            }
            .addOnFailureListener { e ->
                val msg = buildString {
                    append(e.message ?: "未知錯誤")
                    if (e is StorageException) append(" [code=${e.errorCode}, http=${e.httpResultCode}]")
                }
                android.util.Log.e("Upload", "putFile failed", e)
                toast("上傳失敗：$msg")
                onComplete(null)
            }
    }

    // ======== NEW: 三個下拉規則 → 1..9 分數（保底計算） ========
    private fun heuristicBcs(ribs: String, waist: String, stomach: String): Int {
        fun ribsPts(): Int = when {
            ribs.contains("看得見") || ribs.contains("清楚")       -> 0
            ribs.contains("容易摸到") || ribs.contains("輕壓可摸到")-> 0
            ribs.contains("稍難") || ribs.contains("需較用力")     -> 1
            ribs.contains("很難") || ribs.contains("摸不到")       -> 3
            else -> 1
        }
        fun waistPts(): Int = when {
            waist.contains("明顯收腰") -> 0
            waist.contains("輕微收腰") -> 1
            waist.contains("無腰身") || waist.contains("圓桶") -> 3
            else -> 1
        }
        fun stomachPts(): Int = when {
            stomach.contains("上提") -> 0
            stomach.contains("平坦") -> 1
            stomach.contains("下垂") -> 3
            else -> 1
        }
        val score = ribsPts() + waistPts() + stomachPts()
        return score.coerceIn(0, 8) + 1 // 轉成 1..9
    }

    // ======== NEW: 融合 AI 與規則（含 AI 失敗/異常的保底） ========
    private fun combineBcs(ai: Int?, confidence: Double?, rule: Int): Pair<Int, String> {
        val aiValid = ai?.takeIf { it in 1..9 } // 只接受 1..9
        if (aiValid == null) {
            return rule.coerceIn(1, 9) to "AI 無回應或分數無效，使用觸摸規則"
        }
        val diff = kotlin.math.abs(aiValid - rule)
        val final = when {
            diff <= 1 -> ((aiValid + rule) / 2.0).let { kotlin.math.round(it).toInt() }
            diff == 2 -> ((aiValid + 2 * rule) / 3.0).let { kotlin.math.round(it).toInt() }
            else      -> rule
        }.coerceIn(1, 9)

        val why = when {
            diff <= 1 -> "AI 與觸摸接近，取平均"
            diff == 2 -> "AI 與觸摸有差距，觸摸權重較高"
            else      -> "AI 與觸摸落差大，採用觸摸規則"
        }
        val note = if (confidence != null) "（AI 信心 ${"%.2f".format(confidence)}）" else ""
        return final to "$why$note"
    }

    // === 只改這個：呼叫後端 → 永遠產生最終分數，不再顯示「無法分析」 ===
    private fun callBcsApi(imageUrl: String, ribsText: String, waistText: String, stomachText: String) {
        val req = BcsRequest(imageUrl = imageUrl, ribs = ribsText, waist = waistText, stomach = stomachText)

        val progress = ProgressBar(this).apply { isIndeterminate = true }
        val dlg = android.app.AlertDialog.Builder(this)
            .setTitle("分析中…")
            .setView(progress)
            .setCancelable(false)
            .show()

        // 規則分數：任何情況都可當保底
        val ruleScore = heuristicBcs(ribsText, waistText, stomachText)
        android.util.Log.d("BCS", "RuleScore=$ruleScore | ribs=$ribsText, waist=$waistText, stomach=$stomachText")

        api.getBcsScore(req).enqueue(object : retrofit2.Callback<BcsResponse> {
            override fun onResponse(call: retrofit2.Call<BcsResponse>, resp: retrofit2.Response<BcsResponse>) {
                dlg.dismiss()

                // 解析 AI 分數（不在 1..9 一律視為無效）
                val body = resp.body()
                val aiRaw = if (resp.isSuccessful && body != null) body.finalBcs else null
                val ai = aiRaw?.takeIf { it in 1..9 }
                val conf = try { body?.confidence } catch (_: Throwable) { null }

                if (!resp.isSuccessful || body == null) {
                    val errBody = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    android.util.Log.e("API", "HTTP ${resp.code()} body=$errBody")
                } else {
                    android.util.Log.d("API", "AI=$aiRaw (accepted=$ai) conf=$conf")
                }

                // 融合（或保底）
                val (final, why) = combineBcs(ai = ai, confidence = conf, rule = ruleScore)
                android.util.Log.d("BCS", "Final=$final | Reason=$why")
                goToResult(final, imageUrl, why)
            }

            override fun onFailure(call: retrofit2.Call<BcsResponse>, t: Throwable) {
                dlg.dismiss()
                android.util.Log.e("API", "call failed", t)
                val (final, why) = combineBcs(ai = null, confidence = null, rule = ruleScore)
                android.util.Log.d("BCS", "Final=$final | Reason=$why (network failure)")
                goToResult(final, imageUrl, "網路/伺服器失敗，$why")
            }
        })
    }

    // 將最終結果帶回主畫面並切到 BCS 結果頁
    private fun goToResult(finalScore: Int, imageUrl: String, reason: String) {
        // 你原本在 PhotoAndTouchInputActivity 傳進來的狗種（可無視，沒有就空字串）
        val breed = intent.getStringExtra("DOG_BREED") ?: ""

        val go = Intent(this@PhotoAndTouchInputActivity, MainActivity::class.java).apply {
            putExtra("TARGET_FRAGMENT", "BCS_RESULT")
            putExtra("FINAL_BCS_SCORE", finalScore)   // 一定是 1..9
            putExtra("DOG_BREED", breed)
            putExtra("IMAGE_URL", imageUrl)
            putExtra("BCS_REASON", reason)            // 選填：顯示為什麼是這個分數
        }
        startActivity(go)
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
