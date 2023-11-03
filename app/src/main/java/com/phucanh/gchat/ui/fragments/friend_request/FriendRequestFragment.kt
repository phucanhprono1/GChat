package com.phucanh.gchat.ui.fragments.friend_request

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentFriendRequestBinding
import com.phucanh.gchat.models.FriendRequest
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.FriendRequestViewModel

class FriendRequestFragment : Fragment() {

    companion object {
        fun newInstance() = FriendRequestFragment()
    }

    private val viewModel by activityViewModels<FriendRequestViewModel>()
    private lateinit var binding: FragmentFriendRequestBinding
    var friendRequestAdapter: FriendRequestAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRequestBinding.inflate(inflater,container,false)
        return binding.root
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        friendRequestAdapter = FriendRequestAdapter(viewModel.options,object : FriendRequestAdapter.AcceptClickListener{
            override fun onAcceptClick(friendRequest: FriendRequest) {
                viewModel.addFriend(friendRequest.idSender,true)
                viewModel.friendRequestRef.child(StaticConfig.UID).child(friendRequest.idSender).removeValue()
            }
        },object : FriendRequestAdapter.DeleteClickListener{
            override fun onDeleteClick(friendRequest: FriendRequest) {
                viewModel.friendRequestRef.child(StaticConfig.UID).child(friendRequest.idSender).removeValue()
            }
        })
        friendRequestAdapter!!.startListening()
        binding.friendRequestRecyclerView.adapter = friendRequestAdapter
        binding.friendRequestRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.backButtonFriendRequest.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        friendRequestAdapter!!.stopListening()
    }
    override fun onStop() {
        super.onStop()
        friendRequestAdapter!!.stopListening()
    }
}