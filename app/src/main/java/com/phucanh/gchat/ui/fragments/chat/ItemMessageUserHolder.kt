package com.phucanh.gchat.ui.fragments.chat

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.phucanh.gchat.R
import de.hdodenhof.circleimageview.CircleImageView

class ItemMessageUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var txtContent: TextView
    var imgContent: ImageView
    var avata: CircleImageView
    var frameVideo:FrameLayout?=null
    var playerView: PlayerView? = null
    var player: ExoPlayer?=null

    init {
        txtContent = itemView.findViewById<View>(R.id.textContentUser) as TextView
        avata = itemView.findViewById<View>(R.id.imageUser) as CircleImageView
        imgContent = itemView.findViewById<View>(R.id.imageMessageUser) as ImageView
        playerView = itemView.findViewById(R.id.playerViewUser)
        frameVideo=itemView.findViewById(R.id.containerVideoUser)
    }

}