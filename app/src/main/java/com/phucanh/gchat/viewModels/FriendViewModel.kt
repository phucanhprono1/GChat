package com.phucanh.gchat.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val friendDao: FriendDao,
    application: Application
) : AndroidViewModel(application) {
    val _listFriend: MutableLiveData<ListFriend> = MutableLiveData()
    private val friendRef = firebaseDatabase.getReference("friend")
    @Inject
    lateinit var userRef: DatabaseReference
    var listFriendID = ArrayList<String>()

    init {
        // Load friends from Room database on initialization
        getListFriendUId()
        for(i in listFriendID){
            Log.d("FriendViewModel", "init: $i")
        }
    }



    private fun getListFriendUId() {
        friendRef.child(StaticConfig.UID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                    Log.d("FriendViewModel", "onDataChange: ${snapshot.value!!.toString()}")
                    listFriendID.add(snapshot.value!!.toString())
                }
                getAllFriendInfo(0)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }

    fun getAllFriendInfo(index: Int) {

            if (index == listFriendID.size) {
                var listFriend: ListFriend = ListFriend()
                listFriend.listFriend = friendDao.getAll() as ArrayList<Friend>
                // Notify observers that the friend list has been updated
                _listFriend.postValue(listFriend)
            } else {
                val id = listFriendID[index]
                userRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null) {
                            val user: Friend = Friend()
                            val mapUserInfo = dataSnapshot.value as HashMap<*, *>
                            user.user.name = mapUserInfo["name"] as String
                            user.user.email = mapUserInfo["email"] as String
                            user.user.avata = mapUserInfo["avata"] as String
                            user.id = id
                            user.idRoom = if (id.compareTo(StaticConfig.UID) > 0)
                                (StaticConfig.UID + id).hashCode().toString()
                            else
                                (id + StaticConfig.UID).hashCode().toString()
                            friendDao.insert(user)
                        }
                        getAllFriendInfo(index + 1)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle onCancelled event
                    }
                })
            }

    }
}