package com.phucanh.gchat.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(val userReference: DatabaseReference,application: Application) : AndroidViewModel(application) {



    val userList: MutableLiveData<List<User>> = MutableLiveData()

    fun search(name: String) {
        if(name.isEmpty()){
            userList.value = ArrayList()
            return
        }
        else{
            userReference.orderByChild("name")
                .startAt(name.lowercase(Locale.getDefault()))
                .endAt(name.lowercase() + "\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = ArrayList<User>()

                        for (data in snapshot.children) {
                            val user = data.getValue(User::class.java)
                            user?.let { users.add(it) }
                        }
                        userList.value = users
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error
                    }
                })
        }

    }
}