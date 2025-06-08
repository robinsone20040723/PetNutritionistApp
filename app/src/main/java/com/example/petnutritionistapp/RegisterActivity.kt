package com.example.petnutritionistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val backBtn = findViewById<Button>(R.id.backBtn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val pass = passwordInput.text.toString()
            val confirm = confirmPasswordInput.text.toString()

            if (email.isBlank() || pass.isBlank() || confirm.isBlank()) {
                Toast.makeText(this, "請填寫所有欄位", Toast.LENGTH_SHORT).show()
            } else if (pass != confirm) {
                Toast.makeText(this, "兩次密碼不一致", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: 未來這裡串接 Firebase 進行帳號註冊
                Toast.makeText(this, "註冊成功（僅示意）", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        backBtn.setOnClickListener {
            finish() // 返回 LoginActivity
        }
    }
}
