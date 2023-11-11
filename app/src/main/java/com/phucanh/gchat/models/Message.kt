package com.phucanh.gchat.models

data class Message (
    var idSender: String? = null,
    var nameSender: String? = null,
    var idReceiver: String? = null,
    var content: String? = null,
    var type: Int? = 0,
    var timestamp: Long = 0
){

}