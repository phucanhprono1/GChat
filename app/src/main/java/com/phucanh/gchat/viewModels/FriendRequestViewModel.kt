package com.phucanh.gchat.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.phucanh.gchat.models.FriendRequest
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(private val firebaseDatabase: FirebaseDatabase,application: Application): AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val friendRequestRef = firebaseDatabase.getReference("friend_requests")
    init {
        Log.d("FriendRequestViewModel", "init: ${StaticConfig.UID}")
    }
    var options = FirebaseRecyclerOptions.Builder<FriendRequest>()
        .setQuery(friendRequestRef.child(StaticConfig.UID), FriendRequest::class.java)
        .build()
    val friendRef = firebaseDatabase.getReference("friend")
    fun addFriend(idFriend: String?, isIdFriend: Boolean) {
        if (idFriend != null) {
            if (isIdFriend) {
                friendRef.child(StaticConfig.UID).push().setValue(idFriend)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            addFriend(idFriend, false)
                        }
                    }
                    .addOnFailureListener {
                        // handle failure
                    }

            } else {
                friendRef.child(idFriend).push().setValue(StaticConfig.UID)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            addFriend(null, false)
                        }
                    }
                    .addOnFailureListener {
                        // handle failure
                    }
            }
        } else {
            // friend added successfully

        }

    }
}