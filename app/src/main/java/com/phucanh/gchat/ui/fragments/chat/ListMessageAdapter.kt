package com.phucanh.gchat.ui.fragments.chat

import android.content.Context
import android.graphics.Point

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry.getContext
import com.bumptech.glide.Glide

import com.google.firebase.database.DatabaseReference

import com.phucanh.gchat.R

import com.phucanh.gchat.models.Conversation
import com.phucanh.gchat.utils.StaticConfig
import java.util.Objects

class ListMessageAdapter(
    private val context: Context,
    private val conversation: Conversation,
    private val mapAvata: MutableMap<String, String>,
    private val mapAvataUser: String

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var player: ExoPlayer?=null
    private val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
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
//        val playerListener = object: Player.Listener {
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            super.onPlaybackStateChanged(playbackState)
//            when(playbackState){
//                STATE_ENDED -> restartPlayer()
//                STATE_READY -> {
//
//                    play()
//                }
//            }
//        }
//    }
        if (holder is ItemMessageFriendHolder) {

            if(conversation.listMessageData[position].type ==1){
                Glide.with(holder.itemView)
                    .load(conversation.listMessageData[position].content)
                    .override(400, 600)  // width và height là kích thước mới bạn muốn
                    .into(holder.imgContent)
                Glide.with(holder.itemView).load(conversation.listMessageData[position].content).into(holder.imgContent)
                holder.imgContent.visibility = View.VISIBLE
                holder.txtContent.visibility = View.GONE
                holder.playerView?.visibility = View.GONE
            }
            else if (conversation.listMessageData[position].type ==0){
                holder.txtContent.text = conversation.listMessageData[position].content
                holder.txtName.text = conversation.listMessageData[position].nameSender
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.VISIBLE
                holder.playerView?.visibility = View.GONE
            }
            else if(conversation.listMessageData[position].type ==2){
                holder.playerView?.visibility = View.VISIBLE
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.GONE
                val playerListener = object: Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when(playbackState){
                            STATE_ENDED -> restartPlayer()
                            STATE_READY -> {
                                holder.playerView?.player =player
                                play()
                            }
                        }
                    }
                }
                player = ExoPlayer.Builder(context)
                    .build()
                    .apply {

                        val source = conversation.listMessageData[position].content?.let {
                            getHlsMediaSource(
                                it
                            )
                        }

                        setMediaSource(source!!)
                        prepare()
                        addListener(playerListener)
                    }

            }
            val currentAvata = mapAvata[conversation.listMessageData[position].idSender]
            if(currentAvata==null){
                holder.avata.setImageResource(R.drawable.default_avata)
            }
            else{
                Glide.with(holder.itemView).load(currentAvata).into(holder.avata)
            }
        } else if (holder is ItemMessageUserHolder) {
            var player = ExoPlayer.Builder(context).build()

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
            else if(conversation.listMessageData[position].type ==2){
//                player.release()
//                player.playWhenReady = false
                holder.playerView?.visibility = View.VISIBLE
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.GONE
                val playerListener = object: Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when(playbackState){
                            STATE_ENDED -> restartPlayer()
                            STATE_READY -> {
                                holder.playerView?.player =player
                                play()
                            }
                        }
                    }
                }
                player = ExoPlayer.Builder(context)
                    .build()
                    .apply {

                        val source = conversation.listMessageData[position].content?.let {
                            getHlsMediaSource(
                                it
                            )
                        }

                        setMediaSource(source!!)
                        prepare()
                        addListener(playerListener)
                    }
//                val mediaItem = MediaItem.Builder()
//                    .setUri(conversation.listMessageData[position].content)
//                    .setMimeType(MimeTypes.APPLICATION_MP4)
//                    .build()
//                val mediaSystem = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
//                player.setMediaItem(mediaItem)
//                player.prepare()
//                player.play()
//                holder.playerView?.player = player
            }
        }
    }

//    private fun initPlayer(mediaUrl: String){
//        player = ExoPlayer.Builder(context)
//            .build()
//            .apply {
//
//                val source = getHlsMediaSource(mediaUrl)
//
//                setMediaSource(source)
//                prepare()
//                addListener(playerListener)
//            }
//    }

    private fun getHlsMediaSource(mediaUrl: String): MediaSource {
        // Create a HLS media source pointing to a playlist uri.
        return HlsMediaSource.Factory(dataSourceFactory).
        createMediaSource(MediaItem.fromUri(mediaUrl))
    }

//    private fun getProgressiveMediaSource(): MediaSource{
//        // Create a Regular media source pointing to a playlist uri.
//        return ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(Uri.parse(mediaUrl)))
//    }

    private fun releasePlayer(){
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    private fun pause(){
        player?.playWhenReady = false
    }

    private fun play(){
        player?.playWhenReady = true
    }

    private fun restartPlayer(){
        player?.seekTo(0)
        player?.playWhenReady = true
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
