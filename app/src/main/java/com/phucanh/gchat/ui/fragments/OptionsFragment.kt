package com.phucanh.gchat.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentOptionsBinding

import com.phucanh.gchat.models.Configuration
import com.phucanh.gchat.models.User
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.ui.EditProfileActivity
import com.phucanh.gchat.ui.LoginActivity
import com.phucanh.gchat.ui.MainActivity
import com.phucanh.gchat.ui.fragments.friend.FriendFragment
import com.phucanh.gchat.utils.StaticConfig

import com.phucanh.gchat.viewModels.OptionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OptionsFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var myAccount: User
    private lateinit var context: Context

    companion object {
        fun newInstance() = OptionsFragment()
        const val SIGN_OUT ="com.phucanh.gchat.SIGN_OUT"
    }
    private val signoutreceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SIGN_OUT) {
                // Handle the sign out action here
                mAuth.signOut()
                LoginManager.getInstance().logOut()
                Intent().also {
                    it.setClass(requireContext(), LoginActivity::class.java)
                    startActivity(it)
                    activity?.finish()
                    StaticConfig.LIST_FRIEND_ID.clear()
                    viewModel.deleteAllFriend()
                }
            }
        }
    }
    private val viewModel by activityViewModels<OptionsViewModel>()
    private lateinit var binding: FragmentOptionsBinding
    private lateinit var userInfoAdapter: UserInfoAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
        userInfoAdapter = UserInfoAdapter(emptyList(),viewModel.friendDao)
        binding.infoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.infoRecyclerView.adapter = userInfoAdapter
        mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser!=null && isAdded){
            viewModel.userDB.child(mAuth.currentUser!!.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    myAccount = snapshot.getValue(User::class.java)!!
                    binding.tvUsername.text = myAccount.name
                    userInfoAdapter.updateData(viewModel.listConfig(myAccount))
                    if(isAdded){
                        Glide.with(requireContext())
                            .load(myAccount.avata)
                            .apply (RequestOptions().transform(CircleCrop()))
                            .into(binding.imgAvatar)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        val filter = IntentFilter(SIGN_OUT)
        requireContext().registerReceiver(signoutreceiver, filter)


    }
    override fun onDestroyView() {
        super.onDestroyView()
        signoutreceiver.let {
            requireContext().unregisterReceiver(it)
        }
    }
    inner class UserInfoAdapter(private var profileConfig: List<Configuration>,val friendDao: FriendDao) : RecyclerView.Adapter<UserInfoAdapter.ViewHolder>() {
        fun updateData(newData: List<Configuration>) {
            profileConfig = newData
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_info_item_layout, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val config = profileConfig[position]
            holder.label.text = config.label
            holder.value.text = config.value
            holder.icon.setImageResource(config.icon)
            holder.itemView.setOnClickListener {
                if (config.label == getString(R.string.logout)) {
                    Intent().also {
                        it.action = SIGN_OUT
                        requireContext().sendBroadcast(it)
                    }
                }
                if (config.label == getString(R.string.friend_request)){
                    findNavController().navigate(R.id.action_global_friendRequestFragment)
                }



                if (config.label == getString(R.string.change_password)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Password")
                        .setMessage("Are you sure want to reset password?")
                        .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                            resetPassword(myAccount.email)
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                        .show()
                }
                if (config.label == getString(R.string.change_profile)) {
                    val intent = Intent(requireContext(), EditProfileActivity::class.java)
                    intent.putExtra("uid", myAccount.id)
                    startActivity(intent)
                }
            }
        }
        fun resetPassword(email: String?) {
            mAuth.sendPasswordResetEmail(email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Password")
                            .setMessage("Reset password email sent.")
                            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                            .show()
                    } else {
                        AlertDialog.Builder(context)
                            .setTitle("Password")
                            .setMessage("Reset password email failed.")
                            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                            .show()
                    }
                }
        }

        override fun getItemCount(): Int {
            return profileConfig.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val label: TextView = view.findViewById(R.id.tv_title_1)
            val value: TextView = view.findViewById(R.id.tv_detail_1)
            val icon: ImageView = view.findViewById(R.id.img_icon_1)
        }
    }
}