package com.phucanh.gchat.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityMainBinding
import com.phucanh.gchat.models.User
import com.phucanh.gchat.ui.fragments.friend.ListFriendAdapter
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var uid: String = ""
    private lateinit var currentUserDB: DatabaseReference
    private lateinit var friendRequestDB: DatabaseReference
    var detectFriendOnline = object : CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
        override fun onTick(l: Long) {
            ServiceUtils.updateUserStatus(applicationContext)
        }

        override fun onFinish() {}
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            // User is signed in, retrieve UID
            uid = user.uid
            StaticConfig.UID = uid
            currentUserDB = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("/users/$uid")
            currentUserDB = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("/users/$uid")
            currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    StaticConfig.NAME = snapshot.child("name").value.toString()
                    StaticConfig.AVATA = snapshot.child("avata").value.toString()
                    StaticConfig.ADDRESS = snapshot.child("address").value.toString()
                    StaticConfig.BIRTHDAY = snapshot.child("dob").value.toString()
                    StaticConfig.EMAIL = snapshot.child("email").value.toString()
                    StaticConfig.BIO = snapshot.child("bio").value.toString()
                    StaticConfig.JOINEDDATE = snapshot.child("joinedDate").value.toString()
                    StaticConfig.PHONENUMBER = snapshot.child("phonenumber").value.toString()
                    StaticConfig.FCMTOKEN = snapshot.child("fcmToken").value.toString()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
            // ... (other code related to retrieving user information)
        } else {
            // User is signed out
            // Handle the case when the user is not signed in
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
        StaticConfig.UID = uid
        currentUserDB = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("/users/$uid")
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                StaticConfig.NAME = snapshot.child("name").value.toString()
                StaticConfig.AVATA = snapshot.child("avata").value.toString()
                StaticConfig.ADDRESS = snapshot.child("address").value.toString()
                StaticConfig.BIRTHDAY = snapshot.child("dob").value.toString()
                StaticConfig.EMAIL = snapshot.child("email").value.toString()
                StaticConfig.BIO = snapshot.child("bio").value.toString()
                StaticConfig.JOINEDDATE = snapshot.child("joinedDate").value.toString()
                StaticConfig.PHONENUMBER = snapshot.child("phonenumber").value.toString()
                StaticConfig.FCMTOKEN = snapshot.child("fcmToken").value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        detectFriendOnline.start()

        checkNotificationPermission()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = navHostFragment?.findNavController()!!
        navController?.addOnDestinationChangedListener { controller, destination, _ ->

            if (destination.id == R.id.friendRequestFragment  || destination.id == R.id.searchFragment || destination.id == R.id.viewProfileFragment || destination.id==R.id.chatFragment || destination.id == R.id.addGroupFragment) {
                hideBottomNav()

            } else {
                showBottomNav()

            }
        }
        binding.bottomNav.setupWithNavController(navController!!)
        if(navController.currentDestination?.id == R.id.friendFragment || navController.currentDestination?.id == R.id.groupFragment || navController.currentDestination?.id == R.id.optionsFragment){
            showBottomNav()
        }
        else{
            hideBottomNav()
        }

        if(StaticConfig.UID == null || StaticConfig.UID !=mAuth.currentUser?.uid){
            binding.loadingLayout.visibility = View.VISIBLE
            binding.bottomNav.visibility = View.GONE
            binding.navHostFragment.visibility = View.GONE
        }
        else{
            binding.loadingLayout.visibility = View.GONE
            binding.bottomNav.visibility = View.VISIBLE
            binding.navHostFragment.visibility = View.VISIBLE
        }


    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart: ")
        if(navController.currentDestination?.id == R.id.friendFragment || navController.currentDestination?.id == R.id.groupFragment || navController.currentDestination?.id == R.id.optionsFragment){
            showBottomNav()
        }
        else{
            hideBottomNav()
        }



    }
    override fun onPause() {
        Log.d("MainActivity", "onPause: ")
        super.onPause()

    }

    override fun onRestart() {
        super.onRestart()
        Log.d("MainActivity", "onRestart: ")
        detectFriendOnline.start()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
        if(navController.currentDestination?.id == R.id.friendFragment || navController.currentDestination?.id == R.id.groupFragment || navController.currentDestination?.id == R.id.optionsFragment){
            showBottomNav()
        }
        else{
            hideBottomNav()
        }

    }

    override fun onResume() {
        Log.d("MainActivity", "onResume: ")
        super.onResume()
        if(navController.currentDestination?.id == R.id.friendFragment || navController.currentDestination?.id == R.id.groupFragment || navController.currentDestination?.id == R.id.optionsFragment){
            showBottomNav()
        }
        else{
            hideBottomNav()
        }

        detectFriendOnline.start()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp( navController, null)
    }
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(this)
            val isOpened = manager.areNotificationsEnabled()
            if (!isOpened) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Notification Permission")
                builder.setMessage("Please enable notification permission for this app to receive chat notifications.")
                builder.setPositiveButton(
                    "Enable"
                ) { dialog, which -> // Open device settings
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }
        }
    }
    fun hideBottomNav(){
        binding.bottomNav.visibility = View.GONE
    }
    fun showBottomNav(){
        binding.bottomNav.visibility = View.VISIBLE
    }
    override fun onDestroy() {
        super.onDestroy()
        StaticConfig.UID = ""
        StaticConfig.LIST_FRIEND_ID.clear()
        StaticConfig.LIST_FRIEND_REQUEST_ID.clear()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)

        detectFriendOnline.cancel()

    }
    fun finishMain(){
        finish()
    }
}