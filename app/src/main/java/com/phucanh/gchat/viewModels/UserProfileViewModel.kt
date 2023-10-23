package com.phucanh.gchat.viewModels


import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.phucanh.gchat.R
import com.phucanh.gchat.models.Configuration
import com.phucanh.gchat.models.User

class UserProfileViewModel(private val context: Context) : ViewModel() {
    fun listConfig(myAccount: User): List<Configuration> {
        return listOf(
            Configuration(R.mipmap.ic_account_box, context.getString(R.string.username), myAccount.name!!),
            Configuration(R.mipmap.ic_email, context.getString(R.string.email), myAccount.email!!),
            Configuration(R.drawable.ic_friend_request, context.getString(R.string.friend_request),""),
            Configuration(R.mipmap.ic_restore, context.getString(R.string.change_password), ""),
            Configuration(R.drawable.ic_edit_profile, context.getString(R.string.change_profile), ""),
            Configuration(R.drawable.ic_log_out, context.getString(R.string.logout), "")
        )
    }
    val userDB: DatabaseReference = FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("users")


}