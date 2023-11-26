package com.phucanh.gchat.models

import androidx.room.Ignore


data class User(
    var name: String? = null,
    var email: String? = null,
    var avata: String? = null,
    var id: String,
    var joinedDate: String? = null,
    var fcmToken: String? = null,
    var dob: String? = null,
    var address: String? = null,
    var phonenumber: String? = null,
    var bio: String? = null,
    @Ignore
    var message: Message= Message(),
    @Ignore
    var status: Status= Status()
) {
    init {
        status.isOnline = false
        status.timestamp = 0
        message.idReceiver = "0"
        message.idSender = "0"
        message.content = ""
        message.timestamp = 0
    }
    @Ignore
    constructor() : this(
        null,
        null,
        null,
        "",
        null,
        null,
        null,
        null,
        null,
        null
    )
    constructor(
        name: String?,
        email: String?,
        avata: String?,
        id: String,
        joinedDate: String?,
        fcmToken: String?,
        dob: String?,
        address: String?,
        phonenumber: String?,
        bio: String?
    ) : this(
        name,
        email,
        avata,
        id,
        joinedDate,
        fcmToken,
        dob,
        address,
        phonenumber,
        bio,
        Message(),
        Status()
    )
}