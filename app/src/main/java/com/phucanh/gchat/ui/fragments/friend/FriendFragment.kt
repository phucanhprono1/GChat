package com.phucanh.gchat.ui.fragments.friend

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentFriendBinding
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.viewModels.FriendViewModel

class FriendFragment : Fragment() {

    companion object {
        fun newInstance() = FriendFragment()
    }

    private val viewModel by activityViewModels<FriendViewModel>()
    private lateinit var binding: FragmentFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_searchFragment)
        }
        viewModel._listFriend.observe(viewLifecycleOwner) {
            if (it != null) {
                var listFriendsAdapter = ListFriendsAdapter(requireContext(), it, newInstance())
                listFriendsAdapter.notifyDataSetChanged()
                binding.recycleListFriend.adapter = listFriendsAdapter
                binding.recycleListFriend.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            }
        }
    }

}