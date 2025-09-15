package com.example.petnutritionistapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.example.petnutritionistapp.WeightLogFragment   // 依實際 package


class HomeFragment : Fragment() {

    // 位置權限
    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.any { it.value }
        if (granted) openNearbyVets() else openNearbyVetsWithoutLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 對應新版 XML 的 id
        val itemBCS: View = view.findViewById(R.id.itemBCS)
        val itemAI: View = view.findViewById(R.id.itemAI)
        val itemRecord: View = view.findViewById(R.id.itemRecord)      // <- 新增：記錄體重/BCS
        val itemHospital: View = view.findViewById(R.id.itemHospital)
        val btnStartAnalyze: View = view.findViewById(R.id.btnStartAnalyze)

        // 認識 BCS → 介紹頁
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

        // 記錄體重/BCS（由原登出改成開啟紀錄表單頁）
        itemRecord.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WeightLogFragment())
                .addToBackStack("record_bcs")
                .commit()
        }

        // 附近寵物醫院
        itemHospital.setOnClickListener { onNearbyVetClick() }

        // 開始分析狗狗（維持你原本流程）
        btnStartAnalyze.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogInputFragment())
                .addToBackStack(null)
                .commit()
            // 或：startActivity(Intent(requireContext(), PhotoAndTouchInputActivity::class.java))
        }
    }

    private fun onNearbyVetClick() {
        val hasPermission = locationPerms.any {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermission) openNearbyVets() else requestPerms.launch(locationPerms)
    }

    @SuppressLint("MissingPermission")
    private fun openNearbyVets() {
        val ok = locationPerms.any {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (!ok) {
            openNearbyVetsWithoutLocation()
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        val lat = loc.latitude
                        val lng = loc.longitude
                        val query = Uri.encode("寵物醫院")
                        val uri = "geo:$lat,$lng?q=$query&z=15".toUri()
                        openMaps(uri)
                    } else {
                        openNearbyVetsWithoutLocation()
                    }
                }
                .addOnFailureListener { openNearbyVetsWithoutLocation() }
        } catch (se: SecurityException) {
            openNearbyVetsWithoutLocation()
        }
    }

    private fun openNearbyVetsWithoutLocation() {
        val query = Uri.encode("寵物醫院")
        val uri = "geo:0,0?q=$query".toUri()
        openMaps(uri)
    }

    private fun openMaps(uri: Uri) {
        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(mapsIntent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "找不到可開啟地圖的應用程式", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
