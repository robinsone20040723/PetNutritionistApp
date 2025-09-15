package com.example.petnutritionistapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)

        // 先確保「有登入的使用者」
        ensureSignedIn { signedInOk ->
            if (signedInOk) {
                initUi()
            } else {
                // 登入失敗時，不載入主畫面，提示並允許重試
                AlertDialog.Builder(this)
                    .setTitle("登入失敗")
                    .setMessage("目前無法登入，請確認網路或稍後再試。")
                    .setPositiveButton("重試") { _, _ ->
                        ensureSignedIn { ok -> if (ok) initUi() }
                    }
                    .setNegativeButton("關閉") { d, _ -> d.dismiss() }
                    .show()
            }
        }
    }

    /** 確保已登入：若沒有使用者就做匿名登入（只做一次） */
    private fun ensureSignedIn(done: (Boolean) -> Unit) {
        val current = auth.currentUser
        if (current != null) {
            android.util.Log.d("Auth", "Already signed in: ${current.uid}")
            done(true)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener {
                android.util.Log.d("Auth", "Anonymous sign-in success: ${it.user?.uid}")
                done(true)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Auth", "Anonymous sign-in failed", e)
                done(false)
            }
    }

    /** 登入後再初始化整個 UI（載入起始 Fragment、設定 BottomNav） */
    private fun initUi() {
        // ✅ 檢查是否要顯示特定 Fragment（例如從 PhotoAndTouchInputActivity 回來）
        val targetFragment = when (intent.getStringExtra("TARGET_FRAGMENT")) {
            "BCS_RESULT" -> {
                val breed = intent.getStringExtra("DOG_BREED") ?: ""
                val score = intent.getIntExtra("FINAL_BCS_SCORE", -1)
                BCSResultFragment.newInstance(score, breed)
            }
            else -> HomeFragment()
        }
        loadFragment(targetFragment)

        // 📦 設定底部選單邏輯
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_back -> {
                    onBackPressedDispatcher.onBackPressed()
                    true
                }
                R.id.nav_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    // 📦 將指定 Fragment 顯示到畫面上
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 🦾 顯示確認登出對話框
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("確認登出")
            .setMessage("你確定要登出嗎？")
            .setPositiveButton("是") { _, _ -> performLogout() }
            .setNegativeButton("否") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // 🔐 執行登出動作
    private fun performLogout() {
        auth.signOut()

        val sharedPrefs: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
