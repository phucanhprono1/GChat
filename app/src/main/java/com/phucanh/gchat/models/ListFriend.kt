package com.phucanh.gchat.models

class ListFriend {
    var listFriend: ArrayList<Friend>? = null


    fun ListFriend() {
        listFriend = ArrayList()
    }

    fun getAvataById(id: String): String? {
        for (friend in listFriend!!) {
            if (id == friend.id) {
                return friend.avata
            }
        }
        return ""
    }


}