package com.example.petnutritionistapp

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.ortiz.touchview.TouchImageView   // ✅ 改用 TouchImageView

class FullscreenImageDialogFragment : DialogFragment() {

    companion object {
        private const val KEY_RES_ID = "resId"
        fun newInstance(drawableResId: Int) = FullscreenImageDialogFragment().apply {
            arguments = bundleOf(KEY_RES_ID to drawableResId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)

        // 用 TouchImageView（支援縮放/拖曳）
        val imageView = root.findViewById<TouchImageView>(R.id.fullImage)
        val resId = requireArguments().getInt(KEY_RES_ID)
        imageView.setImageResource(resId)

        // 點背景關閉
        root.setOnClickListener { dismiss() }
        return root
    }
}
