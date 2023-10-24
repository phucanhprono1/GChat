package com.phucanh.gchat.ui

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityEditProfileBinding
import com.phucanh.gchat.utils.StaticConfig

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.tvJoinDateEditProfile.text = StaticConfig.JOINEDDATE
        binding.tvUsernameEditInfo.text = StaticConfig.NAME
        binding.edtextUsernameEditInfo.setText(StaticConfig.NAME)
        binding.etEmailEditInfo.text = StaticConfig.EMAIL
        binding.etBirthdayEditInfo.setText(StaticConfig.BIRTHDAY)
        binding.etAddressEditInfo.setText(StaticConfig.ADDRESS)
        binding.etPhoneEditInfo.setText(StaticConfig.PHONENUMBER)
        binding.selfDescriptionEditInfo.setText(StaticConfig.BIO)
        binding.btnEditUsernameEditInfo.setOnClickListener{
            binding.newInfoEditProfileName.visibility = View.VISIBLE
            binding.oldInfoEditProfileName.visibility = View.GONE
        }
        binding.btnDoneUsernameEditInfo.setOnClickListener{
            binding.tvUsernameEditInfo.text = binding.edtextUsernameEditInfo.text
            binding.newInfoEditProfileName.visibility = View.GONE
            binding.oldInfoEditProfileName.visibility = View.VISIBLE
        }
        binding.etBirthdayEditInfo.setText(StaticConfig.BIRTHDAY)
        binding.etBirthdayEditInfo.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                binding.etBirthdayEditInfo.setText("$dayOfMonth/${month+1}/$year")
            }
            val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, dayOfMonth)

            datePickerDialog.show()
        }
        binding.etEmailEditInfo.text = StaticConfig.EMAIL
        binding.etAddressEditInfo.setText(StaticConfig.ADDRESS)
        binding.selfDescriptionEditInfo.setText(StaticConfig.BIO)
        val uid = mAuth.currentUser?.uid
        val currentUserDB = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("/users/$uid")
        binding.btnSaveEditInfo.setOnClickListener{
            currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = binding.tvUsernameEditInfo.text.toString()
                    val email = binding.etEmailEditInfo.text.toString()
                    val birthday = binding.etBirthdayEditInfo.text.toString()
                    val address = binding.etAddressEditInfo.text.toString()
                    val bio = binding.selfDescriptionEditInfo.text.toString()
                    val phone = binding.etPhoneEditInfo.text.toString()
                    currentUserDB.child("name").setValue(name)
                    currentUserDB.child("email").setValue(email)
                    currentUserDB.child("phonenumber").setValue(phone)
                    currentUserDB.child("dob").setValue(birthday)
                    currentUserDB.child("address").setValue(address)
                    currentUserDB.child("bio").setValue(bio)
                    finish()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }
}