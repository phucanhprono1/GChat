package com.phucanh.gchat.ui.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.phucanh.gchat.databinding.FragmentUserProfileBinding
import com.phucanh.gchat.models.Configuration
import com.phucanh.gchat.models.User
import com.phucanh.gchat.ui.EditProfileActivity
import com.phucanh.gchat.ui.LoginActivity
import com.phucanh.gchat.ui.MainActivity
import com.phucanh.gchat.viewModels.UserProfileViewModelFactory
import com.phucanh.gchat.viewModels.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var myAccount: User
    private lateinit var context: Context
    @Inject
    lateinit var viewModelFactory: UserProfileViewModelFactory
    companion object {
        fun newInstance() = UserProfileFragment()
    }

    private lateinit var viewModel: UserProfileViewModel
    val activity: MainActivity? = getActivity() as MainActivity?
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var userInfoAdapter: UserInfoAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this,viewModelFactory).get(UserProfileViewModel::class.java)
        // TODO: Use the ViewModel
        userInfoAdapter = UserInfoAdapter(emptyList())
        binding.infoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.infoRecyclerView.adapter = userInfoAdapter
        mAuth = FirebaseAuth.getInstance()
        viewModel.userDB.child(mAuth.currentUser!!.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myAccount = snapshot.getValue(User::class.java)!!
                userInfoAdapter.updateData(viewModel.listConfig(myAccount))
                Glide.with(requireContext())
                    .load(myAccount.avata)
                    .apply (RequestOptions().transform(CircleCrop()))
                    .into(binding.imgAvatar)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
    inner class UserInfoAdapter(private var profileConfig: List<Configuration>) : RecyclerView.Adapter<UserInfoAdapter.ViewHolder>() {
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
                    FirebaseAuth.getInstance().signOut()
                    LoginManager.getInstance().logOut()
//                FriendDB.getInstance(context).dropDB()
//                GroupDB.getInstance(context).dropDB()
//                ServiceUtils.stopServiceFriendChat(context.applicationContext, true)
                    activity?.finish()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
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
        private fun changeUserName(newName: String) {

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