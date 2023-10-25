package com.phucanh.gchat.models

class ListFriend {
    var listFriend: ArrayList<Friend>? = null

    fun getListFriend(): ArrayList<Friend>? {
        return listFriend
    }

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

    fun setListFriend(listFriend: ArrayList<Friend>?) {
        this.listFriend = listFriend
    }
}