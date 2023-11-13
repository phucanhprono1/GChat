package com.phucanh.gchat.ui.fragments.group.add_group

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentAddGroupBinding
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.AddGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class AddGroupFragment : Fragment(), ListPeopleAdapter.FriendSelectionListener {

    companion object {
        fun newInstance() = AddGroupFragment()
        var listIDChoose = HashSet<String>()
        var listIDRemove = HashSet<String>()
    }
    @Inject
    lateinit var storageReference: StorageReference
    private val viewModel by activityViewModels<AddGroupViewModel>()
    private lateinit var binding: FragmentAddGroupBinding
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddGroupBinding.inflate(inflater,container,false)
        return binding.root
    }
    private lateinit var adapter: ListPeopleAdapter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getListFriend()
        viewModel._listFriend.observe(viewLifecycleOwner){
            adapter = ListPeopleAdapter(requireContext(),it, binding.btnAddGroup,viewModel.listIDChoose,viewModel.listIDRemove,viewModel.isEditGroup,viewModel.group,this)
            binding.recycleListFriend.adapter = adapter
            binding.recycleListFriend.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        }
        if(arguments?.getString("idGroup")!=null){
            viewModel.isEditGroup = true
            viewModel.idGroup = arguments?.getString("idGroup")
            binding.txtActionName.text = "Save"
            binding.editGroupName.setText(arguments?.getString("nameGroup"))
            if(isAdded){
                Glide.with(requireContext()).load(arguments?.getString("avatarGroup")).into(binding.imageGroup)
            }

        }
        binding.linearLayoutavt1.setOnClickListener {
            showImagePickerDialog(requireContext())
        }
        viewModel.nameGroup = binding.editGroupName.text.toString()
        viewModel.listIDChoose.add(StaticConfig.UID)
        binding.btnAddGroup.setOnClickListener {

            if(viewModel.listIDChoose.size>=3 && viewModel.nameGroup!=null && viewModel.avatarGroup!=null){
                viewModel.nameGroup = binding.editGroupName.text.toString()
                viewModel.addGroup()
                viewModel.listIDChoose.clear()
                viewModel.listIDRemove.clear()
                viewModel.avatarGroup = null
                viewModel.avatarGroupUri = null
                viewModel.nameGroup = null
                viewModel.isEditGroup = false
                viewModel.group = null
                findNavController().popBackStack()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }
    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, StaticConfig.CAMERA_REQUEST_CODE)
        }
    }
    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            StaticConfig.STORAGE_PERMISSION_REQUEST_CODE
        )
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, you can proceed with using the camera.
            // Do something with the camera.
        } else {
            // Permission is not granted, request it.
            requestCameraPermission()
        }
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA),
            StaticConfig.CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Override onRequestPermissionsResult to handle the result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StaticConfig.CAMERA_PERMISSION_REQUEST_CODE) {
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
        if (requestCode == StaticConfig.STORAGE_PERMISSION_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            viewModel.avatarGroupUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, viewModel.avatarGroupUri)
                binding.imageGroup.setImageBitmap(bitmap)
                val progressDialog = ProgressDialog(requireContext())
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
                    Toast.makeText(requireActivity(), "Uploaded", Toast.LENGTH_SHORT).show()
                    storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                        viewModel.avatarGroup = uri.toString()
                        viewModel.avatarGroupUri = uri
                    })
                }).addOnFailureListener(OnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(requireActivity(), "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
                }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == StaticConfig.CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap
            binding.imageGroup.setImageBitmap(imageBitmap)

            // Save image to storage and get the filepath
            val progressDialog = ProgressDialog(requireContext())
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
                Toast.makeText(requireActivity(), "Uploaded", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                    viewModel.avatarGroup = uri.toString()
                    viewModel.avatarGroupUri = uri
                })
            }).addOnFailureListener(OnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(requireActivity(), "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            })
        }
    }

    override fun onFriendSelected(id: String) {
        viewModel.listIDChoose.add(id)
        viewModel.listIDRemove.remove(id)
    }

    override fun onFriendDeselected(id: String) {
        viewModel.listIDRemove.add(id)
        viewModel.listIDChoose.remove(id)
    }
}