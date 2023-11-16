package com.phucanh.gchat.ui.fragments.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R

import com.phucanh.gchat.models.Conversation
import com.phucanh.gchat.utils.StaticConfig

class ListMessageAdapter(
    private val context: Context,
    private val conversation: Conversation,
    private val mapAvata: MutableMap<String, String>,
    private val mapAvataUser: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val bitmapAvataDB= mutableMapOf<String, DatabaseReference>()
    fun updateData(conversation: Conversation) {

        this.conversation.listMessageData = conversation.listMessageData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatFragment.VIEW_TYPE_FRIEND_MESSAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_friend_message, parent, false)
                ItemMessageFriendHolder(view)
            }
            ChatFragment.VIEW_TYPE_USER_MESSAGE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_user_message, parent, false)
                ItemMessageUserHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemMessageFriendHolder) {

            if(conversation.listMessageData[position].type ==1){
                Glide.with(holder.itemView)
                    .load(conversation.listMessageData[position].content)
                    .override(400, 600)  // width và height là kích thước mới bạn muốn
                    .into(holder.imgContent)
                Glide.with(holder.itemView).load(conversation.listMessageData[position].content).into(holder.imgContent)
                holder.imgContent.visibility = View.VISIBLE
                holder.txtContent.visibility = View.GONE
            }
            else if (conversation.listMessageData[position].type ==0){
                holder.txtContent.text = conversation.listMessageData[position].content
                holder.txtName.text = conversation.listMessageData[position].nameSender
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.VISIBLE
            }
            val currentAvata = mapAvata[conversation.listMessageData[position].idSender]
            if(currentAvata==null){
                holder.avata.setImageResource(R.drawable.default_avata)
            }
            else{
                Glide.with(holder.itemView).load(currentAvata).into(holder.avata)
            }
        } else if (holder is ItemMessageUserHolder) {


            Glide.with(holder.itemView).load(StaticConfig.AVATA).into(holder.avata)

            if(conversation.listMessageData[position].type ==1){
                Glide.with(holder.itemView)
                    .load(conversation.listMessageData[position].content)
                    .override(400, 600)  // width và height là kích thước mới bạn muốn
                    .into(holder.imgContent)
                holder.imgContent.visibility = View.VISIBLE
                holder.txtContent.visibility = View.GONE
            }
            else if (conversation.listMessageData[position].type ==0){
                holder.txtContent.text = conversation.listMessageData[position].content
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (conversation.listMessageData[position].idSender == StaticConfig.UID) {
            ChatFragment.VIEW_TYPE_USER_MESSAGE
        } else {
            ChatFragment.VIEW_TYPE_FRIEND_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return conversation.listMessageData.size
    }
}
