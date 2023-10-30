package com.phucanh.gchat.ui.fragments.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.phucanh.gchat.R
import com.phucanh.gchat.models.User
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(
    private val list: List<User>,
    private val context: Context,
    val navController: NavController
):RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    inner class SearchViewHolder(
        val view: View,
        val context: Context,
        listener: OnClickListener
    ):RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                listener.onClick(bindingAdapterPosition)
            }
        }
        val avatar = view.findViewById<CircleImageView>(R.id.search_people_item_avatar)
        val name = view.findViewById<TextView>(R.id.search_people_item_name)
    }
    private lateinit var mListener: OnClickListener
    fun setOnClickListener(listener: OnClickListener){
        mListener = listener
    }
    interface OnClickListener{
        fun onClick(position: Int)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_people_item,parent,false)
        return SearchViewHolder(view,context,mListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val user = list[position]
        holder.name.text = user.name
        Glide.with(context).load(user.avata).apply(RequestOptions.circleCropTransform()).into(holder.avatar)
    }

}