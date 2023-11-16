package com.phucanh.gchat.viewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
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
    val _listGroup :MutableLiveData<ArrayList<Group>?> = MutableLiveData()
    var listGroup = ArrayList<Group>()
    val groupRef = firebaseDatabase.getReference("group")
    var index =0
    fun addMemberToGroup(group: Group){

            for(id in group.members){
                if (id !=null){
                    groupDao.insert(GroupMember(id = id,group=group))
                }

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
                    getGroupInfo(index)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    fun deleteGroup(group: Group, index: Int) {
        if (index == group.members.size) {
            firebaseDatabase.reference
                .child("group/${group.id}")
                .removeValue()
                .addOnCompleteListener { task ->
                    groupDao.deleteGroup(group.id)
                    listGroup.remove(group)
                    _listGroup.value = listGroup
                    Toast.makeText(getApplication(), "Deleted group", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e -> }
        } else {
            firebaseDatabase.reference
                .child("users/${group.members[index]}/group/${group.id}")
                .removeValue()
                .addOnCompleteListener { task ->
                    deleteGroup(group, index + 1)
                }
                .addOnFailureListener { e -> }
        }
    }
    fun leaveGroup(group: Group) {
        val groupReference = firebaseDatabase.reference.child("group/${group.id}/members")
        groupReference.orderByValue().equalTo(StaticConfig.UID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Toast.makeText(getApplication(), "You are not in this group", Toast.LENGTH_SHORT).show()
                } else {
                    var memberIndex = ""
                    val result = dataSnapshot.value as ArrayList<String>

                    for (i in 0 until result.size) {
                        if (result[i] != null) {
                            memberIndex = i.toString()
                        }
                    }

                    val userReference = firebaseDatabase.reference.child("users")
                        .child(StaticConfig.UID).child("group").child(group.id)

                    userReference.removeValue()

                    groupReference.child(memberIndex).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // waitingLeavingGroup.dismiss()

                                listGroup.remove(group)
                                groupDao.deleteGroup(group.id)
                                _listGroup.value = listGroup
                                Toast.makeText(getApplication(), "Leave group success", Toast.LENGTH_SHORT).show()

                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation
            }
        })
    }
    fun refreshListGroup() {
        Log.d("GroupFragment", "refreshListGroup: ")
        listGroup.clear()
        groupDao.deleteAll()
        _listGroup.value = null
        index = 0
        getListGroup()
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
                            listGroup[indexGroup].members = groupInfo.members ?: ArrayList() // Check for null

                            Log.d("GroupFragment", "onDataChange: ${listGroup[indexGroup].members.toString()}")
                            addMemberToGroup(listGroup[indexGroup])
                            getGroupInfo(indexGroup + 1)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle cancellation
                    }
                })
        }
    }

}