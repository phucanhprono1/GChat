package com.phucanh.gchat.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
@Entity
open class User{
    open var name: String? = null
    open var email: String? = null
    open var avata: String? = null
    open var id: String = ""
    var joinedDate:String? = null
    open var fcmToken: String? = null
    @Ignore
    var message: Message? = null
    @Ignore
    var status: Status? = null
    var dob: String? = null
    var address: String? = null
    var phonenumber: String? = null
    var bio: String? = null
    constructor(id: String, name: String?, email: String?, avt: String?) {
        this.id = id
        this.name = name
        this.email = email
        this.avata = avt
    }
    constructor(id: String, name: String?, email: String?, avt: String?,fcmToken: String?) {
        this.id = id
        this.name = name
        this.email = email
        this.avata = avt
        this.fcmToken = fcmToken
    }

    constructor(id: String, name: String?, email: String?) {
        this.id = id
        this.name = name
        this.email = email
    }

    constructor() {
        status = Status()
        message = Message()
        status?.isOnline = false
        status?.timestamp = 0
        message?.idReceiver = "0"
        message?.idSender = "0"
        message?.text = ""
        message?.timestamp = 0
    }

}