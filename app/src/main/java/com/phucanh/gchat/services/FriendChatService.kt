package com.phucanh.gchat.services

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.phucanh.gchat.R
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.ui.MainActivity
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class FriendChatService : Service() {
    private val TAG = "FriendChatService"

    // Binder given to clients
    val mBinder: IBinder = LocalBinder()
    var mapMark = mutableMapOf<String?, Boolean>()
    var mapQuery = mutableMapOf<String?, Query> ()
    var mapChildEventListenerMap= mutableMapOf<String, ChildEventListener>()
    var mapBitmap= mutableMapOf<String?,String?>()
    var listKey: ArrayList<String> = ArrayList()
    var listFriend: ListFriend? = null
    var listGroup: ArrayList<Group>? = null
    lateinit var updateOnline: CountDownTimer
    @Inject
    lateinit var friendDao: FriendDao



    override fun onCreate() {

        super.onCreate()
        listFriend?.listFriend = friendDao.getAll() as ArrayList<Friend?>

        updateOnline = object : CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            override fun onTick(l: Long) {
                ServiceUtils.updateUserStatus(applicationContext)
            }

            override fun onFinish() {
                // Do nothing
            }
        }
        updateOnline.start()


    }





    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "OnStartService")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "OnBindService")
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()

        updateOnline.cancel()
        Log.d(TAG, "OnDestroyService")
    }

    inner class LocalBinder : Binder() {
        fun getService(): FriendChatService {
            return this@FriendChatService
        }
    }
}
