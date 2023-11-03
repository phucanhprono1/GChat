package com.phucanh.gchat.viewModels


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.phucanh.gchat.R
import com.phucanh.gchat.models.Configuration
import com.phucanh.gchat.models.User
import com.phucanh.gchat.room.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(private val context: Context,val friendDao: FriendDao, application: Application) : AndroidViewModel(application) {
    fun listConfig(myAccount: User): List<Configuration> {
        return listOf(
            Configuration(R.mipmap.ic_email, context.getString(R.string.email), myAccount.email!!),
            Configuration(R.drawable.ic_friend_request, context.getString(R.string.friend_request),""),
            Configuration(R.mipmap.ic_restore, context.getString(R.string.change_password), ""),
            Configuration(R.drawable.ic_edit_profile, context.getString(R.string.change_profile), ""),
            Configuration(R.drawable.ic_log_out, context.getString(R.string.logout), "")
        )
    }
    val userDB: DatabaseReference = FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("users")
    fun deleteAllFriend(){
        friendDao.deleteAll()
    }

}