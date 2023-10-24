package com.phucanh.gchat.models

import android.os.Parcel
import android.os.Parcelable

open class User:Parcelable {
    var name: String? = null
    var email: String? = null
    var avata: String? = null
    var id: String? = null
    var joinedDate:String? = null
    var fcmToken: String? = null
    var message: Message? = null
    var status: Status? = null
    var dob: String? = null
    var address: String? = null
    var phonenumber: String? = null
    var bio: String? = null
    constructor(id: String?, name: String?, email: String?, avt: String?) {
        this.id = id
        this.name = name
        this.email = email
        this.avata = avt
    }

    constructor(id: String?, name: String?, email: String?) {
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

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}