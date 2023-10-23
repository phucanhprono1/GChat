package com.phucanh.gchat.utils

import com.phucanh.gchat.models.Friend

object StaticConfig {
    const val REQUEST_CODE_REGISTER = 2000
    const val STR_EXTRA_ACTION_LOGIN = "login"
    const val STR_EXTRA_ACTION_RESET = "resetpass"
    const val STR_EXTRA_ACTION = "action"
    const val STR_EXTRA_USERNAME = "username"
    const val STR_EXTRA_PASSWORD = "password"
    const val STR_DEFAULT_URI = "default"
    var UID = ""
    var EMAIL = ""
    var NAME = ""
    var AVATA = ""
    var CITY = ""
    var COUNTRY = ""
    var PROFESSION = ""
    var BIO = ""

    // TODO only use this UID for debug mode
    // var UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22"
    const val INTENT_KEY_CHAT_FRIEND = "friendname"
    const val INTENT_KEY_CHAT_AVATA = "friendavata"
    const val INTENT_KEY_CHAT_ID = "friendid"
    const val INTENT_KEY_CHAT_ROOM_ID = "roomid"
    const val TIME_TO_REFRESH = 1 * 1000.toLong()
    const val TIME_TO_OFFLINE = 2 * 1000.toLong()
    var ID_FRIEND_REQ: String? = null
    var FRIEND_REQUEST: Friend? = null
    val LIST_FRIEND_ID = ArrayList<String>()

}