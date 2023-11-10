package com.phucanh.gchat.ui.fragments.chat

import android.content.Context
import android.view.LayoutInflater
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
    private val bitmapAvata: MutableMap<String, String>,
    private val bitmapAvataUser: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val bitmapAvataDB= mutableMapOf<String, DatabaseReference>()
    fun updateData(conversation: Conversation) {
        this.conversation.listMessageData = conversation.listMessageData
        notifyDataSetChanged()
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
            holder.txtContent.text = conversation.listMessageData[position].content
            val currentAvata = bitmapAvata[conversation.listMessageData[position].idSender]
            if (currentAvata != null) {
                Glide.with(holder.itemView).load(currentAvata).into(holder.avata)
            } else {
                val id = conversation.listMessageData[position].idSender
                if (bitmapAvataDB[id] == null) {
                    bitmapAvataDB[id] to FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
                            .getReference().child("users/$id/avata")
                    bitmapAvataDB[id]?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val avataStr = dataSnapshot.value as String?
                            if (avataStr != null) {
                                if (avataStr != StaticConfig.AVATA) {
                                    ChatFragment.mapAvataFriend.put(id!!, avataStr)
                                } else {
                                    ChatFragment.mapAvataFriend.put(id!!, StaticConfig.AVATA)
                                }
                                notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle onCancelled event
                        }
                    })
                }
            }
        } else if (holder is ItemMessageUserHolder) {
            holder.txtContent.text = conversation.listMessageData[position].content
            if (bitmapAvataUser != null) {
                Glide.with(holder.itemView).load(StaticConfig.AVATA).into(holder.avata)
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
