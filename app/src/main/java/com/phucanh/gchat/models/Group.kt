package com.phucanh.gchat.models

import androidx.room.Entity
import androidx.room.Ignore


data class Group (
    var id: String,
    var name: String? = null,
    var avatar: String? = null,
    var admin: String? = null,
){
    @Ignore
    var listFriend: ListFriend? = null
    @Ignore
    var members: ArrayList<String>? = null
}