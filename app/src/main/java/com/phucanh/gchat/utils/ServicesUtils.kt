package com.phucanh.gchat.utils

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context

import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.IBinder
import com.google.firebase.database.*
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.services.FriendChatService

object ServiceUtils {

    private var connectionServiceFriendChatForStart: ServiceConnection? = null
    private var connectionServiceFriendChatForDestroy: ServiceConnection? = null

    fun isServiceFriendChatRunning(context: Context): Boolean {
        val serviceClass = FriendChatService::class.java
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun stopServiceFriendChat(context: Context, kill: Boolean) {
        if (isServiceFriendChatRunning(context)) {
            val intent = Intent(context, FriendChatService::class.java)
            if (connectionServiceFriendChatForDestroy != null) {
                context.unbindService(connectionServiceFriendChatForDestroy!!)
            }
            connectionServiceFriendChatForDestroy = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    val binder = service as FriendChatService.LocalBinder
                    binder.getService().stopSelf()
                }

                override fun onServiceDisconnected(arg0: ComponentName) {}
            }
            context.bindService(intent,
                connectionServiceFriendChatForDestroy as ServiceConnection, Context.BIND_NOT_FOREGROUND)
        }
    }


    fun startServiceFriendChat(context: Context) {
        if (!isServiceFriendChatRunning(context)) {
            val myIntent = Intent(context, FriendChatService::class.java)
            context.startService(myIntent)
        } else {
            if (connectionServiceFriendChatForStart != null) {
                context.unbindService(connectionServiceFriendChatForStart!!)
            }
            connectionServiceFriendChatForStart = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    val binder = service as FriendChatService.LocalBinder
                    for (friend in binder.getService().listFriend?.listFriend!!) {
                        binder.getService().mapMark[friend?.idRoom] = true
                    }
                }

                override fun onServiceDisconnected(arg0: ComponentName) {}
            }
            val intent = Intent(context, FriendChatService::class.java)
            context.bindService(intent,
                connectionServiceFriendChatForStart as ServiceConnection, Context.BIND_NOT_FOREGROUND)
        }
    }

    fun updateUserStatus(context: Context) {
        if (isNetworkConnected(context)) {
            val uid = StaticConfig.UID
            if (uid != "") {
                FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users/$uid/status/isOnline")
                    .setValue(true)
                FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users/$uid/status/timestamp")
                    .setValue(System.currentTimeMillis())
            }
        }
    }

    fun updateFriendStatus(context: Context, listFriend: ListFriend?) {
        if (isNetworkConnected(context) && listFriend != null && listFriend.listFriend != null) {
            for (friend in listFriend.listFriend!!) {
                val fid = friend?.id
                FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users/$fid/status")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value != null) {
                                val mapStatus = dataSnapshot.value as HashMap<*, *>
                                if (mapStatus["isOnline"] as Boolean && (System.currentTimeMillis() - (mapStatus["timestamp"] as Long) > StaticConfig.TIME_TO_OFFLINE)) {
                                    FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                        .getReference("users/$fid/status/isOnline")
                                        .setValue(false)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }
    }


    fun isNetworkConnected(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        } catch (e: Exception) {
            return true
        }
    }
}
