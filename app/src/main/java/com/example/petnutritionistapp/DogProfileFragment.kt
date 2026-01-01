package com.example.petnutritionistapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment

class DogProfileFragment : Fragment(R.layout.fragment_dog_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== 取得畫面元件 =====
        val spinnerDogType = view.findViewById<Spinner>(R.id.spinnerDogType)
        val spinnerBreed = view.findViewById<Spinner>(R.id.spinnerBreed)
        val spinnerBCS = view.findViewById<Spinner>(R.id.spinnerBCS)

        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val etAge = view.findViewById<EditText>(R.id.etAge)
        val etAllergy = view.findViewById<EditText>(R.id.etAllergy)

        val switchVaccine = view.findViewById<Switch>(R.id.switchVaccine)
        val btnGenerateMeal = view.findViewById<Button>(R.id.btnGenerateMeal)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        // ===== Spinner 資料 =====
        val dogTypeList = listOf(
            "請選擇犬型",
            "小型犬",
            "中型犬",
            "大型犬"
        )

        val breedList = listOf(
            "請選擇犬種",
            "吉娃娃", "博美", "馬爾濟斯", "約克夏",
            "雪納瑞", "米克斯", "貴賓犬", "臘腸犬",
            "法國鬥牛犬", "比熊犬", "西施犬",
            "柴犬", "邊境牧羊犬",
            "黃金獵犬", "哈士奇", "拉布拉多"
        )

        val bcsList = listOf(
            "請選擇 BCS",
            "1～2（過瘦）",
            "3～4（偏瘦）",
            "5（適中）",
            "6～7（過重）",
            "8～9（肥胖）"
        )

        // ===== Spinner Adapter =====
        spinnerDogType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            dogTypeList
        )

        spinnerBreed.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            breedList
        )

        spinnerBCS.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            bcsList
        )

        // ===== 按下「產生專屬配餐」=====
        btnGenerateMeal.setOnClickListener {

            val dogType = spinnerDogType.selectedItem.toString()
            val breed = spinnerBreed.selectedItem.toString()
            val bcs = spinnerBCS.selectedItem.toString()
            val weightText = etWeight.text.toString()
            val ageText = etAge.text.toString()

            // ===== 防呆 =====
            if (
                dogType.startsWith("請選擇") ||
                breed.startsWith("請選擇") ||
                bcs.startsWith("請選擇") ||
                weightText.isBlank() ||
                ageText.isBlank()
            ) {
                Toast.makeText(requireContext(), "請完整填寫狗狗資料", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val allergy = etAllergy.text.toString().ifBlank { "無" }
            val vaccineStatus =
                if (switchVaccine.isChecked) "已完成基礎疫苗"
                else "尚未完成疫苗"

            // ===== Prompt =====
            val prompt = """
                狗狗基本資料如下：
                - 犬型：$dogType
                - 犬種：$breed
                - 體重：$weightText 公斤
                - 年齡：$ageText 歲
                - BCS：$bcs
                - 過敏食物：$allergy
                - 疫苗狀態：$vaccineStatus

                請根據 AAFCO 與一般犬隻營養原則，
                提供「每日配餐建議」，內容需包含：
                1. 主食與蛋白質來源
                2. 碳水與脂肪比例
                3. 應避免的食物
                4. 餵食注意事項（非醫療診斷）

                請以條列方式回覆，語氣專業但易懂。
            """.trimIndent()

            // 🔴 關鍵 Debug 提示
            Toast.makeText(requireContext(), "開始配餐", Toast.LENGTH_SHORT).show()
            tvResult.text = "AI 正在產生專屬配餐建議中，請稍候..."

            btnGenerateMeal.isEnabled = false   // 防止連點

            // ===== 呼叫 GPT =====
            OpenAIService.requestMealPlan(
                prompt = prompt,
                onSuccess = { result ->
                    activity?.runOnUiThread {
                        btnGenerateMeal.isEnabled = true

                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                MealResultFragment.newInstance(result)
                            )
                            .addToBackStack("meal_result")
                            .commit()
                    }
                },
                onError = { error ->
                    activity?.runOnUiThread {
                        btnGenerateMeal.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "GPT 錯誤：$error",
                            Toast.LENGTH_LONG
                        ).show()
                        tvResult.text = "產生失敗：$error"
                    }
                }
            )
        }
    }
}
