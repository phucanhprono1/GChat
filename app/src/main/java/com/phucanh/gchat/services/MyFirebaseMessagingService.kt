package com.phucanh.gchat.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.phucanh.gchat.R
import com.phucanh.gchat.models.NotificationObject
import com.phucanh.gchat.models.User
import com.phucanh.gchat.ui.MainActivity
import com.phucanh.gchat.ui.fragments.friend.ListFriendAdapter
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var chatViewModel: ChatViewModel
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebaseMessagingService", "onNewToken: $token")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification!!.title
        val message = remoteMessage.notification!!.body

        // Hiển thị thông báo
        showNotification(title, message)
    }
    fun stringToCharSequenceList(str: String): ArrayList<CharSequence> {
        // Remove square brackets and split the string by commas
        val elements = str.substring(1, str.length - 1).split(", ")

        // Convert each element to CharSequence and create an ArrayList
        return ArrayList(elements.map { it as CharSequence })
    }

    private fun showNotification(title: String?, message: String?) {
//        val intent1 = Intent("notification_clicked")
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1)
        val noti=Gson().fromJson(message, NotificationObject::class.java)
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        noti.isGroupChat?.let { bundle.putBoolean(StaticConfig.INTENT_KEY_CHAT_IS_GROUP, it) }
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, noti.name)
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_AVATA, noti.avatar)
        bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, noti.roomId)
        bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, stringToCharSequenceList(noti.friendids))

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.main_bottom_nav)
            .setDestination(R.id.chatFragment)
            .setArguments(bundle)
            .createPendingIntent()


        var channel: NotificationChannel? = null
        val CHANNEL_ID = "foreground_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "This is the foreground channel for the app"
        }
        var notificationManager: NotificationManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager!!.createNotificationChannel(channel!!)
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("${noti.name}: ${noti.message}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManagerCompat.notify(1, notificationBuilder.build())
    }
}

