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
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val userReference: DatabaseReference, application: Application) : AndroidViewModel(application) {



    val userList: MutableLiveData<List<User>> = MutableLiveData()

    fun search(name: String) {
        if (name.isEmpty()) {
            userList.value = ArrayList()
            return
        } else {
            val searchKeywords = name.trim().split("\\s+".toRegex())
            val searchResults = mutableListOf<User>()

            userReference.orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if (user != null && user.id != StaticConfig.UID) {
                            val userName = user.name!!.lowercase(Locale.getDefault())

                            if (searchKeywords.any { keyword -> userName.contains(keyword) }) {
                                searchResults.add(user)
                            }
                        }
                    }
                    userList.value = searchResults
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })
        }
    }
}