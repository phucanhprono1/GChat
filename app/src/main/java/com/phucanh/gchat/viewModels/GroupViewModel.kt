package com.phucanh.gchat.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.models.GroupMember
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.room.GroupDao
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel

class GroupViewModel @Inject constructor(val firebaseDatabase: FirebaseDatabase,val userRef: DatabaseReference,friendDao: FriendDao,val groupDao: GroupDao,application: Application): AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val _listGroup :MutableLiveData<ArrayList<Group>> = MutableLiveData()
    var listGroup = ArrayList<Group>()
    val groupRef = firebaseDatabase.getReference("group")
    fun addMemberToGroup(group: Group){
        for(id in group.members){
            groupDao.insert(GroupMember(id = id,group=group))
        }
    }
    fun getListGroup(){
        userRef.child(StaticConfig.UID).child("group").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    val mapListGroup = dataSnapshot.value as HashMap<*, *>
                    val iterator = mapListGroup.keys.iterator()

                    while (iterator.hasNext()) {
                        val idGroup = mapListGroup[iterator.next().toString()] as String
                        val newGroup = Group().apply { id = idGroup }
                        listGroup.add(newGroup)
                    }
                    getGroupInfo(0)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    fun getGroupInfo(indexGroup: Int) {
        if (indexGroup == listGroup.size) {
            _listGroup.value = listGroup
            return
        } else {
            firebaseDatabase.reference.child("group/${listGroup[indexGroup].id}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null) {
                            val groupInfo = dataSnapshot.getValue(Group::class.java)
                            listGroup[indexGroup].name = groupInfo!!.name
                            listGroup[indexGroup].avatar = groupInfo.avatar
                            listGroup[indexGroup].admin = groupInfo.admin
                            listGroup[indexGroup].members = groupInfo.members
                            addMemberToGroup(listGroup[indexGroup])
                            getGroupInfo(indexGroup + 1)
                        }


                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
        }
    }
}