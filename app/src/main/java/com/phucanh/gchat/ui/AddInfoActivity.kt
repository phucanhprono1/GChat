package com.phucanh.gchat.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityAddInfoBinding
import com.phucanh.gchat.models.User

class AddInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddInfoBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        var user= User()
        val uid = mAuth.currentUser?.uid
        binding.etBirthdayAddInfo.setOnClickListener{
            val calendar: Calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                binding.etBirthdayAddInfo.setText("$dayOfMonth/${month+1}/$year")
            }
            val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, dayOfMonth)

            datePickerDialog.show()
        }
        val currentUserDB = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("/users/$uid")
        currentUserDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isDestroyed) { // Kiểm tra xem Activity có bị hủy không
                    val name = snapshot.child("name").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val avt = snapshot.child("avata").value.toString()
                    binding.tvUsernameAddInfo.text = name
                    binding.etEmailAddInfo.setText(email)
                    Glide.with(this@AddInfoActivity)
                        .load(avt)
                        .apply (RequestOptions().transform(CircleCrop()))
                        .into(binding.imgAvatarAddInfo)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        binding.btnSaveAddInfo.setOnClickListener {
            val dob = binding.etBirthdayAddInfo.text.toString()
            val address = binding.etAddressAddInfo.text.toString()
            val phone = binding.etPhoneAddInfo.text.toString()

            user.dob = dob
            user.address = address
            user.phonenumber = phone
            user.bio = binding.selfDescriptionAddInfo.text.toString()
            currentUserDB.child("dob").setValue(user.dob)
            currentUserDB.child("address").setValue(user.address)
            currentUserDB.child("phonenumber").setValue(user.phonenumber)
            currentUserDB.child("bio").setValue(user.bio)
            finish()
        }
    }
}