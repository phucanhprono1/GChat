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

object ServiceUtils {



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
