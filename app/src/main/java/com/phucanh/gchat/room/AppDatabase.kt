package com.phucanh.gchat.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phucanh.gchat.models.Friend

@Database(entities = [Friend::class],version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun friendDao(): FriendDao
}