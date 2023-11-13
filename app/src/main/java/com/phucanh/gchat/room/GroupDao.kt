package com.phucanh.gchat.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phucanh.gchat.models.GroupMember

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(room: GroupMember)
    @Query("SELECT * FROM groupMember")
    fun getAll(): List<GroupMember>
    @Query("SELECT * FROM groupMember WHERE id = :id")
    fun getRoomById(id: String): GroupMember
    @Query("DELETE FROM groupMember WHERE id = :id and group_id = :groupId")
    fun deleteRoomMemberById(id: String, groupId: String)
    @Query("DELETE FROM groupMember WHERE group_id = :groupId")
    fun deleteGroup(groupId: String)
    @Query("DELETE FROM groupMember")
    fun deleteAll()
}