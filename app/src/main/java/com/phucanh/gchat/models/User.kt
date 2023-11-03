package com.phucanh.gchat.models

import androidx.room.Entity
import androidx.room.Ignore

data class User(
    var name: String? = null,
    var email: String? = null,
    var avata: String? = null,
    var id: String = "",
    var joinedDate:String? = null,
    var fcmToken: String? = null,
    var dob: String? = null,
    var address: String? = null,
    var phonenumber: String? = null,
    var bio: String? = null
){

    @Ignore
    var message: Message? = null
    @Ignore
    var status: Status? = null

    @Ignore
    constructor(id: String, name: String?, email: String?, avt: String?) : this() {
        this.id = id
        this.name = name
        this.email = email
        this.avata = avt
    }
    @Ignore
    constructor(id: String, name: String?, email: String?, avt: String?,fcmToken: String?) : this() {
        this.id = id
        this.name = name
        this.email = email
        this.avata = avt
        this.fcmToken = fcmToken
    }
    @Ignore
    constructor(id: String, name: String?, email: String?) : this() {
        this.id = id
        this.name = name
        this.email = email
    }



}