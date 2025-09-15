package com.example.petnutritionistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
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
        if (holder is UserVH) holder.bind(msg)
        if (holder is AIVH) holder.bind(msg)
    }

    override fun getItemCount(): Int = items.size

    fun append(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.lastIndex)
    }

    private class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvMsg)
        fun bind(m: ChatMessage) { tv.text = m.text }
    }

    private class AIVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvMsg)
        fun bind(m: ChatMessage) { tv.text = m.text }
    }
}
