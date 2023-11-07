package com.phucanh.gchat.ui.fragments.friend

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentFriendBinding
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.ui.MainActivity
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import com.phucanh.gchat.viewModels.FriendViewModel

class FriendFragment : Fragment() {

    companion object {
        fun newInstance() = FriendFragment()
        const val DELETE_FRIEND ="com.phucanh.gchat.DELETE_FRIEND"
    }

    private val viewModel by activityViewModels<FriendViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var binding: FragmentFriendBinding
    var listFriendsAdapter: ListFriendsAdapter? = null
    private val deleteFriendReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DELETE_FRIEND) {
                val friendId = intent.getStringExtra("friendId")
                // Handle the delete friend action here
                Log.d("FriendFragment", "onReceive: $friendId")
                viewModel.deleteFriend(friendId!!)
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

        // TODO: Use the ViewModel\
        if (viewModel.listFriendID.size == 0) {
            binding.swipeRefresh.isRefreshing = true
            viewModel.getListFriendUId()
        }
        else {
            binding.swipeRefresh.isRefreshing = false
        }
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_friendFragment_to_searchFragment)

        }



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
            binding.swipeRefresh.isRefreshing = it == null
            if (it != null) {
                listFriendsAdapter = ListFriendsAdapter(requireContext(), it, newInstance(), findNavController())
                listFriendsAdapter!!.notifyDataSetChanged()
                listFriendsAdapter!!.setOnClickListener(object : ListFriendsAdapter.OnClickListener {
                    override fun onClick(position: Int) {
                        val friend = it.listFriend?.get(position)
                        val bundle = Bundle()
                        Log.d("FriendFragment", "onClick: ${friend?.id}")
                        bundle.putString("idFriend", friend?.id)
                        bundle.putString("nameFriend", friend?.user?.name)
                        bundle.putString("avataFriend", friend?.user?.avata)
                        bundle.putString("idRoom", friend?.idRoom)
                        chatViewModel.mapAvatar = HashMap()
                        if(!friend?.user?.avata.equals(StaticConfig.STR_DEFAULT_URI)) {
                            chatViewModel.mapAvatar[friend?.id] to friend?.user?.avata!!
                        }
                        else {
                            chatViewModel.mapAvatar[friend?.id] to StaticConfig.STR_DEFAULT_URI
                        }
                        findNavController().navigate(R.id.action_friendFragment_to_chatFragment, bundle)
                    }

                })
                listFriendsAdapter!!.setOnLongClickListener(object : ListFriendsAdapter.OnLongClickListener {
                    override fun onLongClick(view: View): Boolean {
                        val position = binding.recycleListFriend.getChildAdapterPosition(view)

                        val friend = it.listFriend?.get(position)
                        Log.d("FriendFragment", "onLongClick: ${friend?.id}")
                        showDialogConfirmDeleteFriend(friend?.id, ServiceUtils.isNetworkConnected(requireContext()))
                        return true
                    }

                })
                binding.recycleListFriend.adapter = listFriendsAdapter
                binding.recycleListFriend.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        // Unregister the BroadcastReceiver in the onDestroy method
        requireContext().unregisterReceiver(deleteFriendReceiver)
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
}