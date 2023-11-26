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

class ItemMessageFriendHolder  (itemView: View) : RecyclerView.ViewHolder(itemView) {
    var txtContent: TextView
    var txtName: TextView
    var imgContent: ImageView
    var avata: CircleImageView
    var playerView: PlayerView? = null
    var player: ExoPlayer?=null
    var frameVideo : FrameLayout?=null

    init {
        txtContent = itemView.findViewById<View>(R.id.textContentFriend) as TextView
        avata = itemView.findViewById<View>(R.id.imageFriend) as CircleImageView
        imgContent = itemView.findViewById<View>(R.id.imageMessageFriend) as ImageView
        txtName = itemView.findViewById<View>(R.id.nameFriend) as TextView
        playerView = itemView.findViewById(R.id.playerViewFriend)
        frameVideo=itemView.findViewById(R.id.containerVideoFriend)
    }
}