package com.example.petnutritionistapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var btnBCS: Button
    private lateinit var btnStart: Button
    private lateinit var btnAIAdvisor: Button
    private lateinit var btnLogout: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ✅ 確保這裡對應到的 XML 是 fragment_home.xml
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 🔗 綁定 UI 元件（用 view.findViewById 才對）
        btnBCS = view.findViewById(R.id.btnBCS)
        btnStart = view.findViewById(R.id.btnStart)
        btnAIAdvisor = view.findViewById(R.id.btnAIAdvisor)
        btnLogout = view.findViewById(R.id.btnLogout)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0)

        // 👉 點擊：認識 BCS
        btnBCS.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BCSIntroductionFragment())
                .addToBackStack(null)
                .commit()
        }


        // 👉 點擊：開始分析狗狗
        btnStart.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogInputFragment())
                .addToBackStack(null)
                .commit()
        }


        // 👉 點擊：AI 顧問
        btnAIAdvisor.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AIAdvisorFragment())
                .addToBackStack(null)
                .commit()
        }


        // 👉 點擊：登出
        btnLogout.setOnClickListener {
            auth.signOut()
            sharedPreferences.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
