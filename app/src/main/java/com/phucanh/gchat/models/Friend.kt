package com.phucanh.gchat.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey



data class Friend(

    override var id: String,
    override var name: String?,
    override var email: String?,
    var avt: String,
    override var fcmToken: String?,
    var idRoom: String
):User(id,name,email,avt,fcmToken)