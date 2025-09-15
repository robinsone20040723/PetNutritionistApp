package com.example.petnutritionistapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.petnutritionistapp.ChatMessage


class AIAdvisorFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var edt: EditText
    private lateinit var btn: MaterialButton

    /** 對話資料（會一併送到 OpenAI，讓 AI 記得上下文） */
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    // 你原本就有的東西
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.OPENAI_API_KEY

    // ====== Fragment lifecycle ======
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ai_advisor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv  = view.findViewById(R.id.rvChat)
        edt = view.findViewById(R.id.edtInput)
        btn = view.findViewById(R.id.btnSend)

        adapter = ChatAdapter(messages)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // 讓列表從底部開始
        }

        // 歡迎訊息（可移除）
        appendAi("嗨，我是 AI 顧問。請描述狗狗情況、飲食或想諮詢的問題，我會提出具體建議！")

        btn.setOnClickListener {
            val text = edt.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return@setOnClickListener
            edt.setText("")

            // 1) 先把使用者訊息顯示出來
            appendUser(text)

            // 2) 顯示「思考中…」的暫存訊息
            val loadingIndex = appendAi("正在思考中…")

            // 3) 送去 OpenAI，拿回正式回覆後把暫存訊息更新
            askAI(
                onSuccess = { reply ->
                    messages[loadingIndex] = ChatMessage(reply, fromUser = false)
                    adapter.notifyItemChanged(loadingIndex)
                    scrollToBottom()
                },
                onError = { err ->
                    messages[loadingIndex] = ChatMessage("抱歉，發生錯誤：$err", fromUser = false)
                    adapter.notifyItemChanged(loadingIndex)
                    scrollToBottom()
                }
            )
        }
    }

    // ====== 封裝 UI 操作 ======

    private fun appendUser(text: String) {
        messages.add(ChatMessage(text, fromUser = true))
        adapter.notifyItemInserted(messages.lastIndex)
        scrollToBottom()
    }

    /** 回傳這則訊息在清單中的 index（用來覆蓋「思考中…」） */
    private fun appendAi(text: String): Int {
        messages.add(ChatMessage(text, fromUser = false))
        val idx = messages.lastIndex
        adapter.notifyItemInserted(idx)
        scrollToBottom()
        return idx
    }

    private fun scrollToBottom() {
        rv.post { rv.smoothScrollToPosition(adapter.itemCount - 1) }
    }

    // ====== 呼叫 OpenAI（保留你的 OkHttp 做法，改成帶上下文） ======

    /**
     * 把整段對話（含 system + user/assistant 歷史）送到 OpenAI，
     * 由 API 回傳最新的 AI 回覆字串。
     */
    private fun askAI(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://api.openai.com/v1/chat/completions"

        // 準備 messages：system + 歷史對話
        val messagesArray = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", "你是專業的狗狗營養顧問，回答要清楚、步驟化，必要時用條列說明與數字（如熱量、比例、份量）。")
                }
            )
            // 把目前 RecyclerView 中所有訊息轉成 OpenAI 需要的格式
            messages.forEach { m ->
                put(
                    JSONObject().apply {
                        put("role", if (m.fromUser) "user" else "assistant")
                        put("content", m.text)
                    }
                )
            }
        }

        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo") // 你原本的 model，之後要換再調整
            put("messages", messagesArray)
            put("temperature", 0.7)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("OpenAI", "Key length=${apiKey.length}")
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val content = JSONObject(responseBody)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()

                    withContext(Dispatchers.Main) {
                        onSuccess(content)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("HTTP ${response.code}")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "IO Error")
                }
            }
        }
    }

    // ====== 簡易 Adapter（左右兩種 ViewType） ======

    private class ChatAdapter(
        private val items: MutableList<ChatMessage>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_USER = 1
            private const val VIEW_AI = 2
        }

        override fun getItemViewType(position: Int): Int =
            if (items[position].fromUser) VIEW_USER else VIEW_AI

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inf = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_USER) {
                val v = inf.inflate(R.layout.item_chat_user, parent, false)
                UserVH(v)
            } else {
                val v = inf.inflate(R.layout.item_chat_ai, parent, false)
                AIVH(v)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = items[position]
            when (holder) {
                is UserVH -> holder.bind(msg)
                is AIVH   -> holder.bind(msg)
            }
        }

        override fun getItemCount(): Int = items.size

        private class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tv: android.widget.TextView = itemView.findViewById(R.id.tvMsg)
            fun bind(m: ChatMessage) { tv.text = m.text }
        }

        private class AIVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tv: android.widget.TextView = itemView.findViewById(R.id.tvMsg)
            fun bind(m: ChatMessage) { tv.text = m.text }
        }
    }
}
