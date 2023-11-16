package com.phucanh.gchat.ui.fragments.group

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.values
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentGroupBinding
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.models.User
import com.phucanh.gchat.utils.StaticConfig
import com.phucanh.gchat.viewModels.ChatViewModel
import com.phucanh.gchat.viewModels.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.internal.observers.BasicIntQueueDisposable
import javax.inject.Inject

@AndroidEntryPoint
class GroupFragment : Fragment() {

    companion object {
        fun newInstance() = GroupFragment()
        const val CONTEXT_MENU_DELETE = 1
        const val CONTEXT_MENU_EDIT = 2
        const val CONTEXT_MENU_LEAVE = 3
        const val REQUEST_EDIT_GROUP = 0
        const val CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos"
    }
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase
    private val viewModel by activityViewModels<GroupViewModel>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var binding : FragmentGroupBinding

    var adapter: ListGroupAdapter?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupBinding.inflate(inflater,container,false)
        return binding.root

    }

//    override fun onResume() {
//        super.onResume()
//        viewModel.refreshListGroup()
//    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshListGroup()
            adapter?.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = viewModel.listGroup.size==0
        }
        binding.addGroup.setOnClickListener {
            findNavController().navigate(R.id.action_groupFragment_to_addGroupFragment)
        }
        if(viewModel.listGroup.size==0){
            viewModel.getListGroup()
        }

        viewModel._listGroup.observe(viewLifecycleOwner){
            if(it == null){
                binding.swipeRefresh.isRefreshing = true
                return@observe
            }
            else{
                setFragmentResultListener("addGroupFragmentResult"){_,bundle->
                    if(!bundle.getBoolean("isSuccess")){
                        Log.d("GroupFragment", "onCreat: " + bundle.getBoolean("isSuccess "))
                        viewModel.refreshListGroup()
                        adapter!!.notifyDataSetChanged()
                    }
                }
                binding.swipeRefresh.isRefreshing = false
                viewModel.listGroup = it
               // registerForContextMenu(binding.recycleListGroup)
                adapter = ListGroupAdapter(requireContext(),it)
                adapter!!.notifyDataSetChanged()
                binding.recycleListGroup.adapter = adapter
                binding.recycleListGroup.layoutManager =GridLayoutManager(requireContext(),2)
                adapter!!.setOnItemClickListener(object : ListGroupAdapter.ItemClickListener{
                    override fun onItemClick(position: Int) {
                        val bundle = Bundle()
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, viewModel.listGroup[position].id)
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_FRIEND, viewModel.listGroup[position].name)
                        bundle.putString(StaticConfig.INTENT_KEY_CHAT_AVATA, viewModel.listGroup[position].avatar)
                        var listId: ArrayList<CharSequence> = ArrayList()
                        for(id in viewModel.listGroup[position].members){
                            if(id!=null){
                                listId.add(id)
                                firebaseDatabase.reference.child("users/$id").addValueEventListener(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(User::class.java)
                                        if(user!=null){
                                            chatViewModel.mapAvatar[user.id] = user.avata.toString()
                                            Log.d("GroupFragment", "onDataChange: ${user.avata}")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("GroupFragment", "onCancelled: ${error.message}")
                                    }

                                })
                            }

                        }
                        bundle.putCharSequenceArrayList(StaticConfig.INTENT_KEY_CHAT_ID, listId)
                        findNavController().navigate(R.id.action_global_chatFragment,bundle)
                    }
                })
                adapter!!.setOnBtnMoreClickListener(object : ListGroupAdapter.BtnMoreClickListener {
                    override fun onBtnMoreClick(position: Int, view: View) {
                        val popupMenu = PopupMenu(requireContext(), view)
                        val inflater: MenuInflater = popupMenu.menuInflater
                        inflater.inflate(R.menu.group_context_menu, popupMenu.menu)

                        popupMenu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.menu_edit_group -> {
                                    if (viewModel.listGroup[position].admin == StaticConfig.UID) {
                                        val bundle = Bundle()
                                        bundle.putString("groupId", viewModel.listGroup[position].id)
                                        bundle.putString("groupName", viewModel.listGroup[position].name)
                                        bundle.putString("groupAvatar", viewModel.listGroup[position].avatar)
                                        bundle.putStringArrayList("groupMembers", viewModel.listGroup[position].members)
                                        findNavController().navigate(R.id.action_groupFragment_to_addGroupFragment, bundle)
                                    } else {
                                        Toast.makeText(requireActivity(), "You are not admin", Toast.LENGTH_LONG).show()
                                    }
                                    true
                                }
                                R.id.menu_leave_group -> {
                                    if (viewModel.listGroup[position].admin == StaticConfig.UID) {
                                        Toast.makeText(requireActivity(), "Admin cannot leave group", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.leaveGroup(viewModel.listGroup[position])
                                        adapter!!.notifyDataSetChanged()
                                    }
                                    true
                                }
                                R.id.menu_delete_group -> {
                                    if (viewModel.listGroup[position].admin == StaticConfig.UID) {
                                        val group = viewModel.listGroup[position]
                                        viewModel.listGroup.removeAt(position)
                                        if (group != null) {
                                            viewModel.deleteGroup(group, 0)
                                        }
                                    } else {
                                        Toast.makeText(requireActivity(), "You are not admin", Toast.LENGTH_LONG).show()
                                    }
                                    true
                                }
                                else -> false
                            }
                        }

                        popupMenu.show()
                    }
                })
            }


        }
    }
}