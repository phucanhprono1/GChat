package com.phucanh.gchat.ui.fragments.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentGroupBinding
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.viewModels.GroupViewModel

class GroupFragment : Fragment() {

    companion object {
        fun newInstance() = GroupFragment()
        const val CONTEXT_MENU_DELETE = 1
        const val CONTEXT_MENU_EDIT = 2
        const val CONTEXT_MENU_LEAVE = 3
        const val REQUEST_EDIT_GROUP = 0
        const val CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos"
    }

    private val viewModel by activityViewModels<GroupViewModel>()
    private lateinit var binding : FragmentGroupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupBinding.inflate(inflater,container,false)
        return binding.root
    }
    private lateinit var adapter: ListGroupAdapter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.addGroup.setOnClickListener {
            findNavController().navigate(R.id.action_groupFragment_to_addGroupFragment)
        }
        if(viewModel.listGroup.size==0){
            viewModel.getListGroup()
        }
        viewModel._listGroup.observe(viewLifecycleOwner){
            if(it == null) return@observe
            else{
                adapter = ListGroupAdapter(requireContext(),it,object : ListGroupAdapter.ItemClickListener{
                    override fun onItemClick(group: Group) {
                        val bundle = Bundle()
//                    bundle.putParcelable("group",group)
                        findNavController().navigate(R.id.action_global_chatFragment,bundle)
                    }
                })
                adapter.notifyDataSetChanged()
                binding.recycleListGroup.adapter = adapter
                binding.recycleListGroup.layoutManager =GridLayoutManager(requireContext(),2)
            }


        }
    }

}