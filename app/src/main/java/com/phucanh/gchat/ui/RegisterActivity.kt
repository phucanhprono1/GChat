package com.phucanh.gchat.ui

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityRegisterBinding
import com.phucanh.gchat.models.User
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val CAMERA_PERMISSION_REQUEST_CODE = 1000
    private val CAMERA_REQUEST_CODE = 999
    private val STORAGE_PERMISSION_REQUEST_CODE = 1001
    private val VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    private var filePath: Uri = Uri.EMPTY
    private var avtPath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkCameraPermission()
        storageReference = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        binding.linearLayoutavt.setOnClickListener{
            showImagePickerDialog(this)
        }
        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val repass = binding.repassword.text.toString()
            if (username.isEmpty()) {
                binding.editTextUsername.error = "Username is required."
                return@setOnClickListener
            }

            val matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email)
            if (!matcher.find()) {
                binding.editTextEmail.error = "Email is invalid."
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email is required."
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required."
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.editTextPassword.error = "Password must be >= 6 characters."
                return@setOnClickListener
            }

            if (password != repass) {
                binding.editTextPassword.error = "Password is not match."
                return@setOnClickListener
            }

            if (filePath == Uri.EMPTY) {
                Toast.makeText(this@RegisterActivity, "Please choose your avatar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserToFirebase(username, email)
                        goToMainActivity()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Register failed. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
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
                binding.profileImageRegister.setImageBitmap(bitmap)
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
                    Toast.makeText(this@RegisterActivity, "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
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
            binding.profileImageRegister.setImageBitmap(imageBitmap)

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
                Toast.makeText(this@RegisterActivity, "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            })
        }
    }

    private fun saveUserToFirebase(username: String, email: String) {
        val usersRef = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).reference.child("users")
        val userId = mAuth.currentUser!!.uid

        val user = User(userId, username, email, avtPath)
        usersRef.child(userId).setValue(user)
    }
    private fun goToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}