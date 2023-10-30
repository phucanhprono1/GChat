package com.phucanh.gchat.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentViewProfileBinding
import com.phucanh.gchat.viewModels.ViewProfileViewModel

class ViewProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ViewProfileFragment()
    }

    private val viewModel by activityViewModels<ViewProfileViewModel>()
    private lateinit var binding: FragmentViewProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewProfileBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        // TODO: Use the ViewModel
        val uid = arguments?.getString("uid")
        viewModel.getCurrentUser(uid!!)
        viewModel.chosenuser.observe(viewLifecycleOwner) {
            binding.tvUsernameViewProfile.text = it.name
            binding.tvAddressViewProfile.text = it.address
            binding.tvBirthdayViewProfile.text = it.dob
            binding.tvJoinDateViewProfile.text = it.joinedDate
            binding.selfDescriptionViewProfile.text = it.bio
            binding.tvPhoneViewProfile.text = it.phonenumber
            binding.tvAddressViewProfile.text = it.address
            binding.tvEmailViewProfile.text = it.email

        }
    }

}