package com.phucanh.gchat.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(val userReference: DatabaseReference,application:Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val chosenuser : MutableLiveData<User> = MutableLiveData()
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
}