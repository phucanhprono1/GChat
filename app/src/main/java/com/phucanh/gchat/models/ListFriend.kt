package com.phucanh.gchat.models

import androidx.lifecycle.LiveData

class ListFriend {
    var listFriend: ArrayList<Friend?> = ArrayList()
    var liveListFriend: LiveData<List<Friend>>? = null

    constructor() {
        listFriend = ArrayList()
    }

    fun getAvataById(id: String): String? {
        for (friend in listFriend!!) {
            if (id == friend?.id  ) {
                return friend?.user?.avata
            }
        }
        return ""
    }


}