package com.phucanh.gchat.models

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "roomMember", primaryKeys = ["id", "group_id"])
data class Room(
    var id: String,
    @Embedded(prefix = "group_")
    var group: Group
)
//Room is also member in Group