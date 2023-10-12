package com.phucanh.gchat.models

open class User {
    var name: String? = null
    var email: String? = null
    var avata: String? = null
    var id: String? = null
    var joinedDate = 0L
    var fcmToken: String? = null
    var message: Message? = null
    var status: Status? = null

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
}