package com.example.petnutritionistapp

data class ChatMessage(
    val text: String,
    val fromUser: Boolean,   // true = 使用者, false = AI
    val time: Long = System.currentTimeMillis()
)
