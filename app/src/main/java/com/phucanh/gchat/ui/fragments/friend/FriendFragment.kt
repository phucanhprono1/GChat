package com.phucanh.gchat.ui.fragments.friend

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentFriendBinding
import com.phucanh.gchat.viewModels.FriendViewModel

class FriendFragment : Fragment() {

    companion object {
        fun newInstance() = FriendFragment()
    }

    private lateinit var viewModel: FriendViewModel
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
        viewModel = ViewModelProvider(this).get(FriendViewModel::class.java)
        // TODO: Use the ViewModel
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_searchFragment)
        }

    }

}