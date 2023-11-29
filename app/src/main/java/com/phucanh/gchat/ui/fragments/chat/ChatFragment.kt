package com.phucanh.gchat.ui.fragments.chat

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentChatBinding
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
@UnstableApi @AndroidEntryPoint
class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
        val VIEW_TYPE_USER_MESSAGE = 0
        val VIEW_TYPE_FRIEND_MESSAGE = 1

    }


    var idFriend = ArrayList<CharSequence>()

    private val viewModel by activityViewModels<ChatViewModel>()
    private lateinit var binding: FragmentChatBinding
    private var listMessageAdapter: ListMessageAdapter? = null
    @Inject
    lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            val idlast = idFriend[0]// Lấy giá trị idFriend từ đâu đó
            val bundle = Bundle()
            bundle.putString("idFriend", idlast.toString())
            setFragmentResult("chatFragmentResult", bundle)
            findNavController().popBackStack()
            arguments = null
            viewModel.listMessage.value = null
            viewModel.listMessage.removeObservers(viewLifecycleOwner)
            listMessageAdapter = null

        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val uid = arguments?.getString("uid")
        idFriend = arguments?.getCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID)!!
        val nameFriend = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_FRIEND)
        val avataFriend = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_AVATA)
        viewModel.roomId = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID)!!
        Glide.with(requireContext()).load(avataFriend).apply(RequestOptions.circleCropTransform()).into(binding.chatAvatar)
        viewModel.roomName = nameFriend!!
        binding.chatName.text = nameFriend
        binding.backButtonChat.setOnClickListener {

            val idlast = idFriend[0]// Lấy giá trị idFriend từ đâu đó
            val bundle = Bundle()
            bundle.putString("idFriend", idlast.toString())
            setFragmentResult("chatFragmentResult", bundle)
//            if(idFriend.size>2){
//                findNavController().navigate(R.id.action_chatFragment_to_groupFragment, bundle)
//            }
//            else{
//                findNavController().navigate(R.id.action_chatFragment_to_friendFragment, bundle)
//            }
            findNavController().popBackStack()
            arguments = null
            listMessageAdapter = null
            viewModel.listMessage.value = null
            viewModel.listMessage.removeObservers(viewLifecycleOwner)
        }
        Log.d("ChatFragment", "onActivityCreated: ${viewModel.mapAvatar.toString()}")

       // listMessageAdapter = ListMessageAdapter(requireContext(), null, mapAvataFriend, StaticConfig.AVATA)
        viewModel.getListMessage(viewModel.roomId!!)
        viewModel.listMessage.observe(viewLifecycleOwner) {
            if (it != null){
                listMessageAdapter = ListMessageAdapter(requireContext(), it, viewModel.mapAvatar, avataFriend!!)
              //  listMessageAdapter!!.updateData(it)
                listMessageAdapter!!.notifyDataSetChanged()
                binding.recyclerChat.adapter = listMessageAdapter
                binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false  )
                if(listMessageAdapter!!.itemCount>0){
                    binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)
                }

            }
        }
        binding.btnMoreAction.visibility = View.GONE
        binding.btnChooseVideoMessage.visibility = View.VISIBLE
        binding.btnChooseImageMessage.visibility = View.VISIBLE
        binding.btnMoreAction.setOnClickListener {
            binding.btnMoreAction.visibility = View.GONE
            binding.btnChooseVideoMessage.visibility = View.VISIBLE
            binding.btnChooseImageMessage.visibility = View.VISIBLE
        }
        binding.editWriteMessage.setOnClickListener {
            if(viewModel.conversation.listMessageData.size>0){
                binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)
            }
            binding.btnMoreAction.visibility = View.VISIBLE
            binding.btnChooseVideoMessage.visibility = View.GONE
            binding.btnChooseImageMessage.visibility = View.GONE
        }
        binding.btnSend.setOnClickListener {
            if(binding.editWriteMessage.text.toString().trim().isNotEmpty()){
                viewModel.sendMessage(binding.editWriteMessage.text.toString().trim(), viewModel.roomId, 0,idFriend!!,null)
                binding.editWriteMessage.setText("")
                if(viewModel.conversation.listMessageData.size>0){
                    binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)
                }

            }
        }
        binding.btnChooseImageMessage.setOnClickListener{
            if(viewModel.conversation.listMessageData.size>0){
                binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)
            }
            showImagePickerDialog(requireContext())
        }
        binding.btnChooseVideoMessage.setOnClickListener{
            if(viewModel.conversation.listMessageData.size>0){
                binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)
            }
            chooseVideo()
        }

        // TODO: Use the ViewModel
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.clear()
        viewModel.listMessage.value = null
        viewModel.listMessage.removeObservers(viewLifecycleOwner)
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
    fun chooseVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            StaticConfig.VIDEO_REQUEST_CODE
        )
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
            viewModel.imgUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, viewModel.imgUri)

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
                        viewModel.imgUri = uri
                        viewModel.imgUriLink = uri.toString()
                        viewModel.sendMessage(viewModel.imgUriLink,viewModel.roomId,1,idFriend!!,fileName)
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
                    viewModel.imgUri = uri
                    viewModel.imgUriLink = uri.toString()
                    viewModel.sendMessage(viewModel.imgUriLink,viewModel.roomId,1,idFriend!!,fileName)
                })
            }).addOnFailureListener(OnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(requireActivity(), "Upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
            }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            })
        }else if (requestCode == StaticConfig.VIDEO_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            viewModel.imgUri = data.data
            try {

                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setTitle("Uploading...")
                progressDialog.show()
                val fileName = "video_" + System.currentTimeMillis() + ".mp4"
                val storageRef = storageReference.child("video/$fileName")

                val uploadTask = storageRef.putFile(data.data!!)
                uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    progressDialog.dismiss()
                    Toast.makeText(requireActivity(), "Uploaded", Toast.LENGTH_SHORT).show()
                    storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri ->
                        viewModel.imgUri = uri
                        viewModel.imgUriLink = uri.toString()
                        viewModel.sendMessage(viewModel.imgUriLink,viewModel.roomId,2,idFriend!!,fileName)
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
        }
    }
}