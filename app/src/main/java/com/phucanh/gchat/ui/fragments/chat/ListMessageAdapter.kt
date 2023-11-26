package com.phucanh.gchat.ui.fragments.chat

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.google.firebase.database.DatabaseReference

import com.phucanh.gchat.R

import com.phucanh.gchat.models.Conversation
import com.phucanh.gchat.utils.StaticConfig

@UnstableApi class ListMessageAdapter(
    private val context: Context,
    private val conversation: Conversation,
    private val mapAvata: MutableMap<String, String>,
    private val mapAvataUser: String

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    var player: ExoPlayer?=null
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

        if (holder is ItemMessageFriendHolder) {

            if(conversation.listMessageData[position].type ==1){
                holder.txtName.text = conversation.listMessageData[position].nameSender
                Glide.with(holder.itemView)
                    .load(conversation.listMessageData[position].content)
                    .override(400, 600)  // width và height là kích thước mới bạn muốn
                    .into(holder.imgContent)
                Glide.with(holder.itemView).load(conversation.listMessageData[position].content).into(holder.imgContent)
                holder.imgContent.visibility = View.VISIBLE
                holder.txtContent.visibility = View.GONE
                holder.playerView?.visibility = View.GONE
                holder.frameVideo?.visibility = View.GONE
            }
            else if (conversation.listMessageData[position].type ==0){
                holder.txtContent.text = conversation.listMessageData[position].content
                holder.txtName.text = conversation.listMessageData[position].nameSender
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.VISIBLE
                holder.playerView?.visibility = View.GONE
                holder.frameVideo?.visibility = View.GONE
            }
            else if(conversation.listMessageData[position].type ==2){
                holder.frameVideo?.visibility = View.VISIBLE
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.GONE
                releasePlayer(holder.player)

                val source = conversation.listMessageData[position].content?.let {
                    getProgressiveMediaSource(it)
                }

                if (source != null) {
                    holder.player = ExoPlayer.Builder(context).build().apply {
                        setMediaSource(source)
                        prepare()
                        addListener(PlayerListener(holder))
                    }
                    holder.playerView?.player = holder.player
                } else {
                    // Handle the case where the media source cannot be loaded
                    Log.e("ListMessageAdapter", "Error loading media source. Display a placeholder or handle it as needed.")
                    // For example, you can display a placeholder image or show an error message
                    holder.imgContent.visibility = View.VISIBLE
                    holder.txtContent.visibility = View.GONE
                    // Set a placeholder image or error message to the imgContent view
                    Glide.with(holder.itemView).load(R.drawable.placeholder_video).into(holder.imgContent)
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
//            var player = ExoPlayer.Builder(context).build()

            Glide.with(holder.itemView).load(StaticConfig.AVATA).into(holder.avata)

            if (conversation.listMessageData[position].type == 1) {
                Glide.with(holder.itemView)
                    .load(conversation.listMessageData[position].content)
                    .override(400, 600)  // width và height là kích thước mới bạn muốn
                    .into(holder.imgContent)
                holder.imgContent.visibility = View.VISIBLE
                holder.txtContent.visibility = View.GONE
                holder.frameVideo?.visibility = View.GONE
            } else if (conversation.listMessageData[position].type == 0) {
                holder.txtContent.text = conversation.listMessageData[position].content
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.VISIBLE
                holder.frameVideo?.visibility = View.GONE
            } else if (conversation.listMessageData[position].type == 2) {
//                player.release()
//                player.playWhenReady = false
                holder.frameVideo?.visibility = View.VISIBLE
                holder.imgContent.visibility = View.GONE
                holder.txtContent.visibility = View.GONE
                releasePlayer(holder.player)
                val source = conversation.listMessageData[position].content?.let {
                    getProgressiveMediaSource(it)
                }
                Log.d("ListMessageAdapter", "Source: ${source}")
                if (source != null) {
                    try {
                        // Check if the source is valid
                        holder.frameVideo?.visibility = View.VISIBLE
                        holder.imgContent.visibility = View.GONE
                        holder.txtContent.visibility = View.GONE
                        holder.player = ExoPlayer.Builder(context).build().apply {
                            setMediaSource(source)
                            prepare()
                            addListener(PlayerListener(holder))
                        }
                        holder.playerView?.player = holder.player
                    } catch (e: Exception) {
                        // Handle the case where the media source is faulty
                        Log.e("ListMessageAdapter", "Error loading media source: ${e.message}")
                        // Display a placeholder or handle it as needed
                        holder.imgContent.visibility = View.VISIBLE
                        holder.txtContent.visibility = View.VISIBLE
                        holder.txtContent.text = conversation.listMessageData[position].fileName
                        holder.frameVideo?.visibility = View.GONE
                        // Set a placeholder image or error message to the imgContent view
                        Glide.with(holder.itemView).load(R.drawable.placeholder_video).override(400, 600).into(holder.imgContent)
                    }
                } else {
                    // Handle the case where the media source cannot be loaded
                    Log.e("ListMessageAdapter", "Error loading media source. Display a placeholder or handle it as needed.")
                    // For example, you can display a placeholder image or show an error message
                    holder.imgContent.visibility = View.VISIBLE
                    holder.txtContent.visibility = View.VISIBLE
                    holder.txtContent.text = conversation.listMessageData[position].fileName
                    holder.frameVideo?.visibility = View.GONE
                    // Set a placeholder image or error message to the imgContent view
                    Glide.with(holder.itemView).load(R.drawable.placeholder_video).override(400, 600).into(holder.imgContent)
                }

            }
        }
    }
    private fun getProgressiveMediaSource(mediaUrl: String): MediaSource? {
        val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))

        // Access the playback properties
        val playbackProperties = mediaItem.playbackProperties
        Log.d("ListMessageAdapter", "PlaybackProperties: ${playbackProperties?.mimeType}")
//
//        // Check if the media type is progressive
//        when (playbackProperties?.mimeType) {
//            "video/mp4",
//            "video/webm",
//            "video/ogg",
//            "audio/mpeg",
//            "audio/ogg",
//                // Add other progressive media types here
//            -> {
                return ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
//            }
//            else -> {
//                // Media item is not of a progressive type
//                // Handle accordingly or return null
//                return null
//            }
//        }
    }
    private fun releasePlayer(player: ExoPlayer?) {
        player?.apply {
            playWhenReady = false
            release()
        }
    }
//    fun releaseAllPlayer(){
//        releasePlayer(player)
//    }

//    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
//        if (holder is ItemMessageFriendHolder) {
//            releasePlayer(holder.player)
//        } else if (holder is ItemMessageUserHolder) {
//            releasePlayer(holder.player)
//        }
//        super.onViewRecycled(holder)
//    }

    inner class PlayerListener(private val holder: RecyclerView.ViewHolder) : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if(holder is ItemMessageFriendHolder){
                Log.d(TAG, "Playback state changed: ${holder.player?.playbackState}")
                when (playbackState) {
                    STATE_BUFFERING -> {
                        // Buffering state
                        Log.d(TAG, "Buffering")
                        // Handle buffering
                    }
                    Player.STATE_IDLE -> {
                        // Player is idle
                        Log.d(TAG, "Idle")
                        // Handle idle state
                    }
                    Player.STATE_ENDED -> pause(holder)
                    Player.STATE_READY -> {
                        // Playback started
                        Log.d(TAG, "Ready")
                        // Handle play/pause

                        // Check if the source is rendering video content
                        val mediaItemCount = holder.player?.mediaItemCount ?: 0
                        Log.d(TAG, "MediaItemCount: $mediaItemCount")
                        if (mediaItemCount > 0) {
                            Log.d(TAG, "Video is rendering")
                        } else {
                            Log.d(TAG, "No video content")
                        }
                    }
                }
            }
            else if(holder is ItemMessageUserHolder){
                Log.d(TAG, "Playback state changed: ${holder.player?.playbackState}")
                when (playbackState) {
                    STATE_BUFFERING -> {
                        // Buffering state
                        Log.d(TAG, "Buffering")
                        // Handle buffering
                    }
                    Player.STATE_READY -> {
                        // Playback started
                        Log.d(TAG, "Ready")
                        // Handle play/pause

                        // Check if the source is rendering video content
                        val mediaItemCount = holder.player?.mediaItemCount ?: 0
                        Log.d(TAG, "MediaItemCount: $mediaItemCount")
                        if (mediaItemCount > 0) {
                            Log.d(TAG, "Video is rendering")
                        } else {
                            Log.d(TAG, "No video content")
                        }
                    }
                    Player.STATE_IDLE -> {
                        // Player is idle
                        Log.d(TAG, "Idle")
                        // Handle idle state
                    }
                    Player.STATE_ENDED -> pause(holder)

            }

            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            // Handle playback errors here
            when (error.errorCode) {
                PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> {
                    // Source error
                    Log.e(TAG, "Source error: ${error.errorCodeName}")
                    // Handle source error here
                }
                ExoPlaybackException.ERROR_CODE_DECODING_FAILED -> {
                    // Renderer error
                    Log.e(TAG, "Renderer error: ${error.errorCodeName}")
                    // Handle renderer error here
                }
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    // Unexpected error
                    Log.e(TAG, "Unexpected error: ${error.errorCodeName}")
                    // Handle unexpected error here
                }
            }
        }
    }

    private fun play(holder: RecyclerView.ViewHolder) {
        (holder as? ItemMessageFriendHolder)?.player?.playWhenReady = true
        (holder as? ItemMessageUserHolder)?.player?.playWhenReady = true
    }
    fun pause(holder: RecyclerView.ViewHolder) {
        (holder as? ItemMessageFriendHolder)?.player?.playWhenReady = false
        (holder as? ItemMessageUserHolder)?.player?.playWhenReady = false
    }
    private fun restartPlayer(holder: RecyclerView.ViewHolder) {
        (holder as? ItemMessageFriendHolder)?.player?.seekTo(0)
        (holder as? ItemMessageUserHolder)?.player?.seekTo(0)
        play(holder)
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
