package com.example.androidworkshop9.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidworkshop9.model.Message

class ChatAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(0, message)
        notifyItemInserted(0)
    }
}

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(message: Message) {
        (itemView as TextView).apply {
            textAlignment = when (message.aligment) {
                Message.Aligment.Start -> View.TEXT_ALIGNMENT_VIEW_START
                Message.Aligment.End -> View.TEXT_ALIGNMENT_VIEW_END
                Message.Aligment.Center -> View.TEXT_ALIGNMENT_CENTER
            }
            text = message.value
        }
    }
}
