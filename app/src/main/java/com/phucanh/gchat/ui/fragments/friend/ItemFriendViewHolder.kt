package com.phucanh.gchat.ui.fragments.friend

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.phucanh.gchat.R
import de.hdodenhof.circleimageview.CircleImageView

class ItemFriendViewHolder(
    context: Context,
    view: View,
    listener: ListFriendAdapter.OnClickListener,
    longListener: ListFriendAdapter.OnLongClickListener
) : RecyclerView.ViewHolder(view) {
    val avata: CircleImageView = itemView.findViewById(R.id.icon_avata)
    val txtName: TextView = itemView.findViewById(R.id.txtName)
    val txtTime: TextView = itemView.findViewById(R.id.txtTime)
    val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
    val context: Context = context

    init {
        itemView.setOnClickListener {
            listener.onClick(bindingAdapterPosition)
            txtName.typeface = Typeface.DEFAULT
            txtMessage.typeface = Typeface.DEFAULT
        }
        itemView.setOnLongClickListener {
            longListener.onLongClick(it)

        }
    }
}