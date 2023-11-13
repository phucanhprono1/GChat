package com.phucanh.gchat.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.GroupMember

@Database(entities = [Friend::class, GroupMember::class],version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun groupDao(): GroupDao
}