package com.example.petnutritionistapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemBCS = view.findViewById<View>(R.id.itemBCS)
        val itemAI = view.findViewById<View>(R.id.itemAI)
        val itemRecord = view.findViewById<View>(R.id.itemRecord)
        val itemHospital = view.findViewById<View>(R.id.itemHospital)
        val itemDogProfile = view.findViewById<View>(R.id.itemDogProfile)
        val btnStartAnalyze = view.findViewById<View>(R.id.btnStartAnalyze)

        // 認識 BCS
        itemBCS.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BCSIntroductionFragment())
                .addToBackStack(null)
                .commit()
        }

        // AI 顧問
        itemAI.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AIAdvisorFragment())
                .addToBackStack(null)
                .commit()
        }

        // 體重 / BCS 紀錄
        itemRecord.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WeightLogFragment())
                .addToBackStack(null)
                .commit()
        }

        // 狗狗簡章
        itemDogProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // 附近寵物醫院（不使用 GPS，直接搜尋）
        itemHospital.setOnClickListener {
            openNearbyVets()
        }

        // 開始分析狗狗
        btnStartAnalyze.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogInputFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // ================== 地圖（穩定版） ==================

    private fun openNearbyVets() {
        val query = Uri.encode("寵物醫院")
        val uri = "geo:0,0?q=$query".toUri()
        openMaps(uri)
    }

    private fun openMaps(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "找不到可開啟地圖的應用程式",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
