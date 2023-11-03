package com.phucanh.gchat.models

data class FriendRequest(
    var idSender: String,
    var idReceiver: String,
    var nameSender: String,
    var imageSender: String,
    var status: String,
    var createdAt:String,
    var idRoom: String
){
    constructor():this("","","","","","","")
}
