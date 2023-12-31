package com.phucanh.gchat.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentViewProfileBinding
import com.phucanh.gchat.models.User
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ViewProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ViewProfileFragment()
    }
    @Inject
    lateinit var userRef : DatabaseReference
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
        userRef.child(uid!!).get().addOnSuccessListener {
            viewModel.chosenuser = it.getValue(User::class.java)!!
            viewModel.chosenuser.let {
                binding.tvUsernameViewProfile.text = it.name
                binding.tvAddressViewProfile.text = it.address
                binding.tvBirthdayViewProfile.text = it.dob
                binding.tvJoinDateViewProfile.text = it.joinedDate
                binding.selfDescriptionViewProfile.text = it.bio
                binding.tvPhoneViewProfile.text = it.phonenumber
                binding.tvAddressViewProfile.text = it.address
                binding.tvEmailViewProfile.text = it.email
                Glide.with(requireContext()).load(it.avata).apply (RequestOptions.circleCropTransform()).into(binding.profileImageViewProfile)

            }
        }
        //viewModel.getCurrentUser(uid!!)
//        viewModel.getCurrentUser(uid!!).let{
//            binding.tvUsernameViewProfile.text = it.name
//            binding.tvAddressViewProfile.text = it.address
//            binding.tvBirthdayViewProfile.text = it.dob
//            binding.tvJoinDateViewProfile.text = it.joinedDate
//            binding.selfDescriptionViewProfile.text = it.bio
//            binding.tvPhoneViewProfile.text = it.phonenumber
//            binding.tvAddressViewProfile.text = it.address
//            binding.tvEmailViewProfile.text = it.email
//            Glide.with(requireContext()).load(it.avata).apply (RequestOptions.circleCropTransform()).into(binding.profileImageViewProfile)
//        }
        binding.backButtonViewProfile.setOnClickListener {
            findNavController().popBackStack()
        }
        if (StaticConfig.LIST_FRIEND_ID.contains(uid)) {
            binding.btnAddFriendViewProfile.visibility = View.GONE
            binding.btnFriendRequestSentViewProfile.visibility = View.GONE
            binding.btnUnfriendViewProfile.visibility = View.VISIBLE
            binding.btnMessageViewProfile.visibility = View.VISIBLE
        }
        else {
            binding.btnAddFriendViewProfile.visibility = View.VISIBLE
            binding.btnFriendRequestSentViewProfile.visibility = View.GONE
            binding.btnUnfriendViewProfile.visibility = View.GONE
            binding.btnMessageViewProfile.visibility = View.GONE
        }
        if(viewModel.checkFriendRequest(uid)){
            binding.btnAddFriendViewProfile.visibility = View.GONE
            binding.btnFriendRequestSentViewProfile.visibility = View.VISIBLE
            binding.btnUnfriendViewProfile.visibility = View.GONE
            binding.btnMessageViewProfile.visibility = View.GONE
        }
        binding.btnAddFriendViewProfile.setOnClickListener {
            viewModel.addFriendRequest(uid)
            viewModel.sendPushNotification(uid,StaticConfig.NAME+" "+ getString(R.string.sent_you_a_friend_request))
            binding.btnAddFriendViewProfile.visibility = View.GONE
            binding.btnFriendRequestSentViewProfile.visibility = View.VISIBLE
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

}