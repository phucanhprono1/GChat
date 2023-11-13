package com.phucanh.gchat.models

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "groupMember", primaryKeys = ["id", "group_id"])
data class GroupMember(
    var id: String,
    @Embedded(prefix = "group_")
    var group: Group
)
