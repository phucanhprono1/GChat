package com.phucanh.gchat.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.FragmentSearchBinding
import com.phucanh.gchat.viewModels.SearchViewModel

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private val viewModel by activityViewModels<SearchViewModel>()
    private lateinit var binding: FragmentSearchBinding
    var searchAdapter: SearchAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query!!)

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText!!)
                return false
            }

        })
        binding.btnBackSearch.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        viewModel.userList.observe(viewLifecycleOwner) {
            searchAdapter = SearchAdapter(it!!, requireContext(),findNavController())
            searchAdapter!!.updateList(it)
            searchAdapter!!.setOnClickListener(object : SearchAdapter.OnClickListener {
                override fun onClick(position: Int) {
                    val args = Bundle()
                    val model = it?.get(position)
                    args.putString("uid", model!!.id)
                    findNavController().navigate(
                        R.id.action_searchFragment_to_viewProfileFragment,
                        args
                    )
                }
            })
            binding.recycleListSearch.adapter = searchAdapter
            binding.recycleListSearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        }
    }
    override fun onDestroy() {
        super.onDestroy()
        searchAdapter = null
    }


}


