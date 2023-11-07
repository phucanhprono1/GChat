package com.phucanh.gchat.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
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
import com.phucanh.gchat.models.FriendRequest
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var uid: String = ""
    private lateinit var currentUserDB: DatabaseReference
    private lateinit var friendRequestDB: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
        StaticConfig.UID = uid
        checkNotificationPermission()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment?.findNavController()
        binding.bottomNav.setupWithNavController(navController!!)
        navController?.addOnDestinationChangedListener { controller, destination, _ ->
            if (destination.id == R.id.friendRequestFragment  || destination.id == R.id.searchFragment || destination.id == R.id.viewProfileFragment || destination.id==R.id.chatFragment) {
                hideBottomNav()
            } else {
                showBottomNav()
            }
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
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onRestart() {
        super.onRestart()

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
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onResume() {
        super.onResume()

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
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
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

    }
    fun finishMain(){
        finish()
    }
}