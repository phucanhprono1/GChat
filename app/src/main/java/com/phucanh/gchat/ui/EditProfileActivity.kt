package com.phucanh.gchat.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityEditProfileBinding
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.utils.StaticConfig.CAMERA_PERMISSION_REQUEST_CODE
import com.phucanh.gchat.utils.StaticConfig.CAMERA_REQUEST_CODE
import com.phucanh.gchat.utils.StaticConfig.STORAGE_PERMISSION_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var mAuth: FirebaseAuth
    @Inject
    lateinit var storageReference: StorageReference
    private var filePath: Uri = Uri.EMPTY
    private var avtPath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this).load(StaticConfig.AVATA)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profileImageEditProfile)
        mAuth = FirebaseAuth.getInstance()
        binding.linearLayoutAvatarEditProfile.setOnClickListener{
            showImagePickerDialog(this)
        }
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
                    val avatar = avtPath
                    if (filePath != Uri.EMPTY) {
                        currentUserDB.child("avata").setValue(avatar)
                        currentUserDB.child("name").setValue(name)
                        currentUserDB.child("email").setValue(email)
                        currentUserDB.child("phonenumber").setValue(phone)
                        currentUserDB.child("dob").setValue(birthday)
                        currentUserDB.child("address").setValue(address)
                        currentUserDB.child("bio").setValue(bio)
                    }
                    else{
                        currentUserDB.child("name").setValue(name)
                        currentUserDB.child("email").setValue(email)
                        currentUserDB.child("phonenumber").setValue(phone)
                        currentUserDB.child("dob").setValue(birthday)
                        currentUserDB.child("address").setValue(address)
                        currentUserDB.child("bio").setValue(bio)
                    }
                    finish()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }
    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }
    }
    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), STORAGE_PERMISSION_REQUEST_CODE)
    }
    fun showImagePickerDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_image_picker)
        1
        val captureButton = dialog.findViewById<ImageButton>(R.id.imgCamera)
        val galleryButton = dialog.findViewById<ImageButton>(R.id.imgGallery)
        val cancelButton = dialog.findViewById<ImageButton>(R.id.imgCancel)

        captureButton.setOnClickListener {
            // Xử lý chức năng chụp ảnh
            // Ví dụ: Gọi Intent để mở máy ảnh
            checkCameraPermission()
            takePicture()
        }

        galleryButton.setOnClickListener {
            // Xử lý chức năng chọn ảnh từ thư viện
            // Ví dụ: Gọi Intent để mở thư viện ảnh
            chooseImage()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, you can proceed with using the camera.
            // Do something with the camera.
        } else {
            // Permission is not granted, request it.
            requestCameraPermission()
        }
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    // Override onRequestPermissionsResult to handle the result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted. You can proceed with using the camera.
                // Do something with the camera.
            } else {
                // Camera permission denied. You may want to show a message or take other action.
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.profileImageEditProfile.setImageBitmap(bitmap)
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()
                val fileName = "avatar_" + System.currentTimeMillis() + ".jpg"
                val storageRef = storageReference.child("avatar/$fileName")
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val dataBytes = baos.toByteArray()
                val uploadTask = storageRef.putBytes(dataBytes)
                uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                        avtPath = uri.toString()
                        filePath = uri
                    })
                }).addOnFailureListener(OnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@EditProfileActivity, "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
                }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap
            binding.profileImageEditProfile.setImageBitmap(imageBitmap)

            // Save image to storage and get the filepath
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            val fileName = "avatar_" + System.currentTimeMillis() + ".jpg"
            val storageRef = storageReference.child("avatar/$fileName")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataBytes = baos.toByteArray()
            val uploadTask = storageRef.putBytes(dataBytes)
            uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                    avtPath = uri.toString()
                    filePath = uri
                })
            }).addOnFailureListener(OnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this@EditProfileActivity, "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            })
        }
    }
}