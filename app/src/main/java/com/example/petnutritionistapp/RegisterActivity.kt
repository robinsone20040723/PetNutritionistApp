package com.example.petnutritionistapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val backBtn = findViewById<Button>(R.id.backBtn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirm = confirmPasswordInput.text.toString()

            if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
                Toast.makeText(this, "請填寫所有欄位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "兩次密碼不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid == null) {
                        Toast.makeText(this, "註冊成功但無法取得使用者 UID", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    // 準備 users/{uid} 初始文件（對應 Firestore Rules）
                    val userDoc = hashMapOf(
                        "email" to email,
                        "displayName" to "",                         // 若有暱稱可填
                        "createdAt" to FieldValue.serverTimestamp()  // 由伺服器填時間
                    )

                    db.collection("users")
                        .document(uid)              // ★ 一定要用 uid，不是 email
                        .set(userDoc)
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ 註冊成功！", Toast.LENGTH_SHORT).show()
                            finish() // 返回登入畫面
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "⚠️ 資料寫入失敗：${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ 註冊失敗：${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        backBtn.setOnClickListener { finish() }
    }
}
