package com.phucanh.gchat.models

data class NotificationObject (
    val name: String?,
    val friendids: String,
    val roomId: String?,
    val avatar: String?,
    val message: String?,
    val isChat : Boolean?,
    val isGroupChat: Boolean?,
    val isFriendChat: Boolean?
)