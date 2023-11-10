package com.phucanh.gchat.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phucanh.gchat.models.Room

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(room: Room)
    @Query("SELECT * FROM roomMember")
    fun getAll(): List<Room>
    @Query("SELECT * FROM roomMember WHERE id = :id")
    fun getRoomById(id: String): Room
    @Query("DELETE FROM roomMember WHERE id = :id and group_id = :groupId")
    fun deleteRoomById(id: String, groupId: String)
    @Query("DELETE FROM roomMember")
    fun deleteAll()
}