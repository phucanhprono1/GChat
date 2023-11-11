package com.phucanh.gchat.viewModels

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val context: Context,
    private val firebaseDatabase: FirebaseDatabase,
    private val friendDao: FriendDao,
    application: Application
) : AndroidViewModel(application) {
    val _listFriend: MutableLiveData<ListFriend?> = MutableLiveData()
    var listFriend: ListFriend= ListFriend()

    var index = 0
    private val friendRef = firebaseDatabase.getReference("friend")
    @Inject
    lateinit var userRef: DatabaseReference
    private val friendRequestRef = firebaseDatabase.getReference("friend_requests")
    var listFriendID = ArrayList<String>()

    var detectFriendOnline :CountDownTimer
    init {
        _listFriend.value = listFriend
        getListFriendUId()
        detectFriendOnline = object : CountDownTimer(StaticConfig.TIME_TO_REFRESH, StaticConfig.TIME_TO_REFRESH) {
            override fun onTick(millisUntilFinished: Long) {
                // TODO Auto-generated method stub
                ServiceUtils.updateUserStatus(context)
                ServiceUtils.updateFriendStatus(context, _listFriend.value)
            }

            override fun onFinish() {

            }
        }
    }


    fun refreshListFriend() {
        friendDao.deleteAll()
        StaticConfig.LIST_FRIEND_ID.clear()
        detectFriendOnline.cancel()
        listFriendID.clear()
        index = 0
        getListFriendUId()
    }


    fun getListFriendUId() {
        friendRef.child(StaticConfig.UID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {
                  //  Log.d("FriendViewModel", "onDataChange: ${snapshot.value!!.toString()}")
                    listFriendID.add(snapshot.value!!.toString())
                    StaticConfig.LIST_FRIEND_ID.add(snapshot.value!!.toString())
                }
                getAllFriendInfo(index)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }


    fun getAllFriendInfo(index: Int) {

            if (index == listFriendID.size) {
                listFriend!!.listFriend = friendDao.getAll() as ArrayList<Friend?>
                _listFriend.value = listFriend
                detectFriendOnline.start()
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
    fun deleteFriend(idFriend: String) {
        Log.d("FriendViewModel", "deleteFriend: $idFriend")
        friendRef.child(StaticConfig.UID)
            .orderByValue().equalTo(idFriend)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        // Friend not found
                    } else {

                        val idRemoval = (dataSnapshot.value as HashMap<*, *>).keys.iterator().next().toString()
                        Log.d("FriendViewModel", "idRemoval: $idRemoval")
                        friendRef.child(idFriend).orderByValue().equalTo(StaticConfig.UID).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.value == null) {
                                    // Friend not found
                                } else {
                                    val idRemoval2 = (dataSnapshot.value as HashMap<*, *>).keys.iterator().next().toString()
                                    Log.d("FriendViewModel", "idRemoval2: $idRemoval2")
                                    friendRef.child(StaticConfig.UID).child(idRemoval).removeValue()
                                    friendRef.child(idFriend).child(idRemoval2).removeValue()

                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle onCancelled event
                            }
                        })

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        friendDao.deleteFriendById(idFriend)

        // Xóa bạn từ LiveData
        val currentListFriend = _listFriend.value?.listFriend?.toMutableList()
        currentListFriend?.removeIf { it?.id  == idFriend }
        val updatedListFriend = ListFriend()
        updatedListFriend.listFriend = currentListFriend as ArrayList<Friend?>
        _listFriend.value = updatedListFriend

        // Xóa bạn từ listFriendID
        listFriendID.remove(idFriend)

        // Xóa bạn từ StaticConfig.LIST_FRIEND_ID
        StaticConfig.LIST_FRIEND_ID.remove(idFriend)
    }
}