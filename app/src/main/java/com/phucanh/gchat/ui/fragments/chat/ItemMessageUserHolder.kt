package com.phucanh.gchat.ui.fragments.chat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.phucanh.gchat.R
import de.hdodenhof.circleimageview.CircleImageView

class ItemMessageUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var txtContent: TextView
    var imgContent: ImageView
    var avata: CircleImageView

    init {
        txtContent = itemView.findViewById<View>(R.id.textContentUser) as TextView
        avata = itemView.findViewById<View>(R.id.imageUser) as CircleImageView
        imgContent = itemView.findViewById<View>(R.id.imageMessageUser) as ImageView
    }
}