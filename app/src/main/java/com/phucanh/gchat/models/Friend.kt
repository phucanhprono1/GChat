package com.phucanh.gchat.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "friends")
data class Friend(

    var idRoom: String,
    @Embedded(prefix = "user_")
    var user: User
){
    @PrimaryKey(autoGenerate = false)
    var id = user.id
    @Ignore
    constructor() : this(
        "",
        User()
    )
}