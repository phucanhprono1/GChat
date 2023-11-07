package com.phucanh.gchat.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend


@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(friend: Friend)

    @Query("SELECT * FROM friends")
    fun getAll(): List<Friend>

    @Query("SELECT * FROM friends")
    fun getAllLive(): LiveData<List<Friend>>

    @Query("SELECT * FROM friends WHERE id = :id")
    fun getFriendById(id: String): Friend

    @Query("DELETE FROM friends WHERE id = :id")
    fun deleteFriendById(id: String)

    @Query("DELETE FROM friends")
    fun deleteAll()
}
