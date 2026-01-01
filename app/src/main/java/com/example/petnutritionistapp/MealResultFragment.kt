package com.example.petnutritionistapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class MealResultFragment : Fragment(R.layout.fragment_meal_result) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMealResult = view.findViewById<TextView>(R.id.tvMealResult)

        // 接收 AI 回傳結果
        val result = arguments?.getString("meal_result") ?: "無法取得配餐建議"

        tvMealResult.text = result
    }

    companion object {
        fun newInstance(result: String): MealResultFragment {
            val fragment = MealResultFragment()
            fragment.arguments = Bundle().apply {
                putString("meal_result", result)
            }
            return fragment
        }
    }
}
