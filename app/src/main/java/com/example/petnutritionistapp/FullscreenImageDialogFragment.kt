package com.example.petnutritionistapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

class FullscreenImageDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_RES = "image_res"

        fun newInstance(imageResId: Int): FullscreenImageDialogFragment {
            val fragment = FullscreenImageDialogFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE_RES, imageResId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)
        val imageView = view.findViewById<ImageView>(R.id.fullscreenImage)

        val imageRes = arguments?.getInt(ARG_IMAGE_RES) ?: 0
        if (imageRes != 0) {
            imageView.setImageResource(imageRes)
        }

        imageView.setOnClickListener {
            dismiss()
        }

        return view
    }
}
