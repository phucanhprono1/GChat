package com.phucanh.gchat.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "friends")
data class Friend(

    var idRoom: String,
    @Embedded(prefix = "user_")
    var user: User
){
    @PrimaryKey(autoGenerate = false)
    var id = user.id
}