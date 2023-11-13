package com.phucanh.gchat.viewModels

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.ui.fragments.friend.FriendFragment
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
    var mapQuery = HashMap<String?, Query?>()
    var mapQueryOnline = HashMap<String?, DatabaseReference?>()
    var mapChildListener = HashMap<String?, ChildEventListener?>()
    var mapChildListenerOnline = HashMap<String?, ChildEventListener>()
    var mapMark = HashMap<String?, Boolean?>()
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

    fun updateMessageAndStatus(listFriend: ListFriend) {
        for(position in 0..listFriend.listFriend!!.size-1){
            var id = listFriend.listFriend!![position]!!.user.id
            var idRoom = listFriend.listFriend!![position]!!.idRoom
            if (mapQuery[id] == null && mapChildListener[id] == null) {
                Log.d("FriendsAdapter", "Message Content: ${listFriend.listFriend!![position]!!.user.message?.content}")
                Log.d("FriendsAdapter", "Message Timestamp: ${listFriend.listFriend!![position]!!.user.message?.timestamp}")
                mapQuery[id] =
                    FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("message")
                        .child(idRoom)
                        .limitToLast(1)

                val childEventListener = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val mapMessage = dataSnapshot.value as HashMap<*, *>?
                        if (mapMark[id] != null) {
                            if (!mapMark[id]!!) {
                                mapMark[id] = true

                                // Update message content
                                listFriend.listFriend!![position]!!.user.message?.content =
                                    "$id${mapMessage!!["content"]}"
                                Log.d("FriendsAdapter", "Message Content: ${listFriend.listFriend!![position]!!.user.message?.content}")

                                // Notify the adapter on the UI thread

                            } else {
                                listFriend.listFriend!![position]!!.user.message?.content =
                                    mapMessage!!["content"] as String
                            }
                        } else {
                            listFriend.listFriend!![position]!!.user.message?.content =
                                mapMessage!!["content"] as String
                        }
                        listFriend.listFriend!![position]!!.user.message?.timestamp = mapMessage["timestamp"] as Long
                    }


                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                mapChildListener[id] = childEventListener
//                    mapQuery[id]?.addChildEventListener(childEventListener)
                mapChildListener[id]?.let { mapQuery[id]?.addChildEventListener(it) }
                Log.d("FriendsAdapter", "mapQuery[$id]: ${mapQuery[id]}")
                mapMark[id] = true
            }
            else {
                mapChildListener[id]?.let { mapQuery[id]?.removeEventListener(it) }
                mapChildListener[id]?.let { mapQuery[id]?.addChildEventListener(it) }
                mapMark[id] = true
            }
            if (mapQueryOnline[id] == null && mapChildListenerOnline[id] == null) {
                mapQueryOnline[id] = FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(id)
                    .child("status")

                mapChildListenerOnline[id] = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {

                            if (listFriend.listFriend!![position]!!.user != null && listFriend.listFriend!![position]!!.user.status == null) {
                                listFriend.listFriend!![position]!!.user.status?.isOnline = dataSnapshot.getValue(Boolean::class.java) ?: false
                                Log.d("FriendsAdapter", "Is Online: ${listFriend.listFriend!![position]!!.user.status?.isOnline}")


                            }

                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {


                            if (listFriend.listFriend!![position]!!.user != null && listFriend.listFriend!![position]!!.user.status != null) {
                                listFriend.listFriend!![position]!!.user.status?.isOnline = dataSnapshot.getValue(Boolean::class.java) ?: false


                            }
                        }
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                mapChildListenerOnline[id]?.let { mapQueryOnline[id]?.addChildEventListener(it) }
            }
        }



    }
    fun refreshListFriend() {
        friendDao.deleteAll()
        StaticConfig.LIST_FRIEND_ID.clear()
        detectFriendOnline.cancel()
        listFriendID.clear()
        index = 0
//        _listFriend.value = null
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
                updateMessageAndStatus(listFriend)
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
        StaticConfig.LIST_FRIEND_ID.remove(idFriend)
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