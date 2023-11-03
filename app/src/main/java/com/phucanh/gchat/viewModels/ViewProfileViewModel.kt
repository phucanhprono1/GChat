package com.phucanh.gchat.viewModels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.FriendRequest
import com.phucanh.gchat.models.User
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(val userReference: DatabaseReference,val firebaseDatabase: FirebaseDatabase,application:Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val chosenuser : MutableLiveData<User> = MutableLiveData()
//    init {
//        checkFriendRequest(StaticConfig.UID)
//    }
    fun getCurrentUser(uid:String){
        userReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    chosenuser.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }
    fun checkFriendRequest(idFriend: String?){
        if (idFriend != null) {
            val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
            val friendRequestRef = firebaseDatabase.getReference("friend_requests")
            friendRequestRef.child(currentUserID).child(idFriend).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val friendRequest = snapshot.getValue(FriendRequest::class.java)
                        if (friendRequest != null) {
                            if (friendRequest.status == "pending") {
                                Toast.makeText(getApplication(), "Friend request sent", Toast.LENGTH_SHORT).show()
                            } else if (friendRequest.status == "accepted") {
                                Toast.makeText(getApplication(), "You are friends", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        addFriendRequest(idFriend)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }
    fun addFriendRequest(idFriend: String?) {
        if (idFriend != null ) {
            val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
            var friendRequest = FriendRequest()
            friendRequest.idReceiver = idFriend
            friendRequest.idSender = currentUserID
            friendRequest.status = "pending"
            friendRequest.nameSender = StaticConfig.NAME
            friendRequest.imageSender = StaticConfig.AVATA
            friendRequest.createdAt = System.currentTimeMillis().toString()
            friendRequest.idRoom= if (idFriend.compareTo(StaticConfig.UID) > 0) (StaticConfig.UID + idFriend).hashCode()
                    .toString() + "" else "" + (idFriend + StaticConfig.UID).hashCode();
            val friendRequestRef = firebaseDatabase.getReference("friend_requests")
            friendRequestRef.child(idFriend).child(currentUserID).setValue(friendRequest)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(getApplication(), "Friend request sent", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "Failed to send friend request",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        getApplication(),
                        "Failed to send friend request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}