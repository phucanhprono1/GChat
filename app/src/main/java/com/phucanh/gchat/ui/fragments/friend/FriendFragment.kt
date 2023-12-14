package com.phucanh.gchat.ui.fragments.friend

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentFriendBinding
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.ui.MainActivity


import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import com.phucanh.gchat.viewModels.FriendViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendFragment : Fragment() {

    companion object {
        fun newInstance() = FriendFragment()
        const val DELETE_FRIEND ="com.phucanh.gchat.DELETE_FRIEND"
        const val CHAT_FRIEND = 12

    }
    @Inject
    lateinit var friendDao: FriendDao
    var listFriend: ListFriend? = null
    var listFriendID= ArrayList<String>()
    @Inject
    lateinit var userRef: DatabaseReference


    private val viewModel by activityViewModels<FriendViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var binding: FragmentFriendBinding
    var listFriendsAdapter: ListFriendAdapter? = null
    private val deleteFriendReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DELETE_FRIEND) {
                val friendId = intent.getStringExtra("friendId")
                // Handle the delete friend action here
                Log.d("FriendFragment", "onReceive: $friendId")
                viewModel.deleteFriend(friendId!!)
                listFriendsAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val idFriend = arguments?.getString("idFriend")
        if(idFriend!= null) {
            ListFriendAdapter.mapMark[idFriend] = null
        }
        setFragmentResultListener("chatFragmentResult") { _, result ->
            val idFriend = result.getString("idFriend")
            if (ListFriendAdapter.mapMark != null) {

                ListFriendAdapter.mapMark[idFriend] = false
            }

        }
        // TODO: Use the ViewModel\
        if (viewModel.listFriendID?.size == 0) {
            binding.swipeRefresh.isRefreshing = true
//            viewModel.getListFriendUId()
            viewModel.refreshListFriend()
            listFriendsAdapter?.notifyDataSetChanged()
        }
        else {
            binding.swipeRefresh.isRefreshing = false
        }
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_searchFragment)

        }
//        if(listFriend == null) {
//            listFriend?.listFriend = friendDao.getAll() as ArrayList<Friend?>
//            if(listFriend?.listFriend!!.size > 0) {
//                for(friend in listFriend?.listFriend!!) {
//                    listFriendID.add(friend?.id!!)
//                    StaticConfig.LIST_FRIEND_ID.add(friend?.id!!)
//                }
//            }
//
//        }
//        if(listFriendID.size == 0) {
//            getListFriendUId()
//        }
//        listFriendsAdapter = ListFriendAdapter(requireContext(), listFriend!!, findNavController())
//        listFriendsAdapter!!.notifyDataSetChanged()
//        listFriendsAdapter!!.setOnClickListener(object : ListFriendAdapter.OnClickListener {
//            override fun onClick(position: Int) {
//                val friend = viewModel.listFriend.listFriend?.get(position)
//                val bundle = Bundle()
//                Log.d("FriendFragment", "onClick: ${friend?.id}")
//                var idFriend: ArrayList<CharSequence> = ArrayList()
//                idFriend.add(friend?.id!!)
//                bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, idFriend)
//                bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, friend?.user?.name)
//                bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, friend?.idRoom)
//                bundle.putString(StaticConfig.INTENT_KEY_CHAT_AVATA, friend?.user?.avata)
//                chatViewModel.mapAvatar = HashMap()
//                if(!friend?.user?.avata.equals(StaticConfig.STR_DEFAULT_URI)) {
//                    chatViewModel.mapAvatar[friend?.id!!] = friend?.user?.avata!!
//                }
//                else {
//                    chatViewModel.mapAvatar[friend?.id!!] = StaticConfig.STR_DEFAULT_URI
//                }
//                findNavController().navigate(R.id.action_global_chatFragment, bundle)
//                ListFriendAdapter.mapMark[friend?.id!!] = null
//            }
//
//        })
//        listFriendsAdapter!!.setOnLongClickListener(object : ListFriendAdapter.OnLongClickListener {
//            override fun onLongClick(view: View): Boolean {
//                val position = binding.recycleListFriend.getChildAdapterPosition(view)
//
//                val friend = viewModel.listFriend.listFriend?.get(position)
//                Log.d("FriendFragment", "onLongClick: ${friend?.id}")
//                showDialogConfirmDeleteFriend(friend?.id, ServiceUtils.isNetworkConnected(requireContext()))
//                return true
//            }
//
//        })
//
//        viewModel.detectFriendOnline.start()

        binding.swipeRefresh.setOnRefreshListener {
            if (ServiceUtils.isNetworkConnected(requireContext())) {
                viewModel.refreshListFriend()

                listFriendsAdapter?.notifyDataSetChanged()
            } else {
                Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_SHORT).show()
            }
            binding.swipeRefresh.isRefreshing = false
        }
        val filter = IntentFilter(DELETE_FRIEND)
        requireContext().registerReceiver(deleteFriendReceiver, filter)

        viewModel._listFriend.observe(viewLifecycleOwner) {
            if(it==null)binding.swipeRefresh.isRefreshing = true
            else{
                viewModel.detectFriendOnline.start()
                binding.swipeRefresh.isRefreshing = false
                listFriendsAdapter = ListFriendAdapter(requireContext(),it, findNavController())
                listFriendsAdapter!!.notifyDataSetChanged()
//                listFriendsAdapter!!.updateData(it)
                listFriendsAdapter!!.setOnClickListener(object : ListFriendAdapter.OnClickListener {
                    override fun onClick(position: Int) {
                        val friend = viewModel.listFriend.listFriend?.get(position)
                        val bundle = Bundle()
                        Log.d("FriendFragment", "onClick: ${friend?.id}")
                        var idFriend: ArrayList<CharSequence> = ArrayList()
                        idFriend.add(friend?.id!!)
                        bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, idFriend)
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, friend?.user?.name)
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, friend?.idRoom)
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_AVATA, friend?.user?.avata)
                        chatViewModel.mapAvatar = HashMap()
                        if(!friend?.user?.avata.equals(StaticConfig.STR_DEFAULT_URI)) {
                            chatViewModel.mapAvatar[friend?.id!!] = friend?.user?.avata!!
                        }
                        else {
                            chatViewModel.mapAvatar[friend?.id!!] = StaticConfig.STR_DEFAULT_URI
                        }
                        findNavController().navigate(R.id.action_global_chatFragment, bundle)
                        ListFriendAdapter.mapMark[friend?.id!!] = null
                    }

                })
                listFriendsAdapter!!.setOnLongClickListener(object : ListFriendAdapter.OnLongClickListener {
                    override fun onLongClick(view: View): Boolean {
                        val position = binding.recycleListFriend.getChildAdapterPosition(view)

                        val friend = viewModel.listFriend.listFriend?.get(position)
                        Log.d("FriendFragment", "onLongClick: ${friend?.id}")
                        showDialogConfirmDeleteFriend(friend?.id, ServiceUtils.isNetworkConnected(requireContext()))
                        return true
                    }

                })
                Log.d("FriendFragment", "onActivityCreated: ${it.listFriend?.size}")
                binding.recycleListFriend.adapter = listFriendsAdapter
                binding.recycleListFriend.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister the BroadcastReceiver in the onDestroy method
        requireContext().unregisterReceiver(deleteFriendReceiver)
    }
    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(deleteFriendReceiver, IntentFilter(DELETE_FRIEND))
    }
    fun showDialogConfirmDeleteFriend(idFriend: String?, connected: Boolean) {

            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Do you want to delete this friend?")
            builder.setPositiveButton("Yes") { _, _ ->
                Intent().also { intent ->
                    intent.action = FriendFragment.DELETE_FRIEND
                    intent.putExtra("friendId", idFriend)
                    requireContext().sendBroadcast(intent)
                }
            }
            builder.setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            builder.show()

    }
//    fun getListFriendUId() {
//        viewModel.friendRef.child(StaticConfig.UID).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                for (snapshot in dataSnapshot.children) {
//                    //  Log.d("FriendViewModel", "onDataChange: ${snapshot.value!!.toString()}")
//                    listFriendID?.add(snapshot.value!!.toString())
//                    StaticConfig.LIST_FRIEND_ID.add(snapshot.value!!.toString())
//                }
//                getAllFriendInfo(0)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle onCancelled event
//            }
//        })
//    }
//
//
//    fun getAllFriendInfo(index: Int) {
//
//        if (index == listFriendID.size) {
//            Log.d("FriendViewModel", "size listFriend: ${listFriendID.size}")
//
//                viewModel.detectFriendOnline.start()
//
//        } else {
//            val id = listFriendID?.get(index)
//            userRef.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    if (dataSnapshot.value != null) {
//                        val user: Friend = Friend()
//                        val mapUserInfo = dataSnapshot.value as HashMap<*, *>
//                        user.user.name = mapUserInfo["name"] as String
//                        user.user.email = mapUserInfo["email"] as String
//                        user.user.avata = mapUserInfo["avata"] as String
//                        user.id = id
//                        user.idRoom = if (id.compareTo(StaticConfig.UID) > 0)
//                            (StaticConfig.UID + id).hashCode().toString()
//                        else
//                            (id + StaticConfig.UID).hashCode().toString()
//                        friendDao.insert(user)
//                    }
//                    getAllFriendInfo(index + 1)
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//                    // Handle onCancelled event
//                }
//            })
//        }
//
//    }
}