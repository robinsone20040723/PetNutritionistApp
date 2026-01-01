package com.example.petnutritionistapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class BCSResultFragment : Fragment() {

    private lateinit var ivDogImage: ImageView
    private lateinit var tvResult: TextView
    private lateinit var tvDiseases: TextView
    private lateinit var btnDisease: Button

    private lateinit var db: FirebaseFirestore

    private var bcsScore: Int = 5        // 1~9
    private var breedName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bcsScore = it.getInt("FINAL_BCS_SCORE", 5)
            breedName = it.getString("DOG_BREED", "")?.trim() ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bcs_result, container, false)

        ivDogImage = view.findViewById(R.id.ivDogImage)
        tvResult = view.findViewById(R.id.tvResult)
        tvDiseases = view.findViewById(R.id.tvDiseases)
        btnDisease = view.findViewById(R.id.btnDisease)

        db = FirebaseFirestore.getInstance()

        showBcsResult()
        loadDogImage()
        loadCommonDiseases()

        btnDisease.setOnClickListener {
            val fragment = DiseaseFragment().apply {
                arguments = Bundle().apply {
                    putString("DOG_BREED", breedName)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    /* =========================
       📊 顯示 BCS 結果
       ========================= */
    private fun showBcsResult() {
        val resultText = when (bcsScore) {
            1, 2 -> "您的狗狗為：超瘦（BCS 1–2 分）"
            3, 4 -> "您的狗狗為：過瘦（BCS 3–4 分）"
            5 -> "您的狗狗為：適中（BCS 5 分）"
            6, 7 -> "您的狗狗為：過重（BCS 6–7 分）"
            8, 9 -> "您的狗狗為：超重（BCS 8–9 分）"
            else -> "無法判斷狗狗體態"
        }
        tvResult.text = resultText
    }

    /* =========================
       🐶 從 Firebase 載入狗狗圖片
       collection: breedImages
       documentId: 品種名稱（博美、柴犬…）
       ========================= */
    private fun loadDogImage() {
        if (breedName.isEmpty()) return

        db.collection("breedImages")
            .document(breedName)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val imageUrl = doc.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .into(ivDogImage)
                    } else {
                        Toast.makeText(requireContext(), "imageUrl 為空", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "找不到品種圖片：$breedName", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "讀取狗狗圖片失敗", Toast.LENGTH_SHORT).show()
            }
    }

    /* =========================
       🩺 從 Firebase 載入常見疾病
       collection: commonDiseases
       ========================= */
    private fun loadCommonDiseases() {
        if (breedName.isEmpty()) return

        db.collection("commonDiseases")
            .document(breedName)
            .get()
            .addOnSuccessListener { doc ->
                val diseases = doc.get("diseases") as? List<String>
                tvDiseases.text =
                    if (!diseases.isNullOrEmpty()) {
                        diseases.joinToString("\n• ", prefix = "• ")
                    } else {
                        "尚無疾病資料"
                    }
            }
            .addOnFailureListener {
                tvDiseases.text = "讀取疾病資料失敗"
            }
    }

    companion object {
        fun newInstance(score: Int, breed: String?): BCSResultFragment {
            val fragment = BCSResultFragment()
            fragment.arguments = Bundle().apply {
                putInt("FINAL_BCS_SCORE", score)
                putString("DOG_BREED", breed)
            }
            return fragment
        }
    }
}
