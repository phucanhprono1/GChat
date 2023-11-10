package com.phucanh.gchat.ui.fragments.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.phucanh.gchat.databinding.FragmentChatBinding
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel

class ChatFragment : Fragment() {

    companion object {
        fun newInstance() = ChatFragment()
        val VIEW_TYPE_USER_MESSAGE = 0
        val VIEW_TYPE_FRIEND_MESSAGE = 1
        var mapAvataFriend= mutableMapOf<String, String>()
    }
    var imgSend: Uri? = null
    var imgSendLink : String? = null



    private val viewModel by activityViewModels<ChatViewModel>()
    private lateinit var binding: FragmentChatBinding
    private var listMessageAdapter: ListMessageAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
            arguments = null
            viewModel.listMessage.value = null

        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val uid = arguments?.getString("uid")
        val idFriend = arguments?.getCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID)
        val nameFriend = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_FRIEND)
        val avataFriend = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_AVATA)
        val roomId = arguments?.getString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID)
        Glide.with(requireContext()).load(avataFriend).apply(RequestOptions.circleCropTransform()).into(binding.chatAvatar)
        binding.chatName.text = nameFriend
        binding.backButtonChat.setOnClickListener {
            findNavController().popBackStack()
            arguments = null
            viewModel.listMessage.value = null
        }
        Log.d("ChatFragment", "onActivityCreated: ${mapAvataFriend.toString()}")
        mapAvataFriend = viewModel.mapAvatar
        viewModel.getListMessage(roomId!!)
        viewModel.listMessage.observe(viewLifecycleOwner) {
            if (it != null){
                listMessageAdapter = ListMessageAdapter(requireContext(), it, viewModel.mapAvatar, avataFriend!!)
                listMessageAdapter!!.updateData(it)
                listMessageAdapter!!.notifyDataSetChanged()
                binding.recyclerChat.adapter = listMessageAdapter
                binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false  )
                binding.recyclerChat.scrollToPosition(listMessageAdapter!!.itemCount - 1)

            }
        }
        binding.btnSend.setOnClickListener {
            viewModel.sendMessage(binding.editWriteMessage.text.toString(), roomId!!, 0,idFriend!!)
            binding.editWriteMessage.setText("")

        }
        binding.btnChooseImageMessage.setOnClickListener{

        }

        // TODO: Use the ViewModel
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.clear()
        viewModel.listMessage.value = null
    }

}