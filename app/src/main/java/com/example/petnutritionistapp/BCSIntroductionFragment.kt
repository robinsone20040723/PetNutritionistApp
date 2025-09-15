package com.example.petnutritionistapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment

class BCSIntroductionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bcs_introduction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 點圖 → 全螢幕可縮放
        view.findViewById<ImageView>(R.id.bcsImage).setOnClickListener {
            FullscreenImageDialogFragment
                .newInstance(R.drawable.bcs_chart_wsava)
                .show(parentFragmentManager, "fullImage")
        }

        // 下一頁（分級說明）→ 切換到 BCSIntroductionFragment2
        view.findViewById<MaterialButton>(R.id.btnNextPage).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BCSIntroductionFragment2()) // ✅ 跳到第二頁
                .addToBackStack(null) // 支援返回上一頁
                .commit()
        }
    }
}
