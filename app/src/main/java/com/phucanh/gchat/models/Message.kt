package com.phucanh.gchat.models

data class Message (
    var idSender: String="",
    var nameSender: String="",
    var idReceiver: String="",
    var content: String="",
    var type: Int? = 0,
    var timestamp: Long = 0,
    var fileName: String=""
)