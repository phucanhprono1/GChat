package com.phucanh.gchat.ui.fragments.group

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phucanh.gchat.R
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.utils.StaticConfig
import de.hdodenhof.circleimageview.CircleImageView

class ListGroupAdapter(private val context: Context,
                       private val listGroup: ArrayList<Group>
                       ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var clickListener : ItemClickListener
    private lateinit var btnMoreClickListener: BtnMoreClickListener
    interface ItemClickListener{
        fun onItemClick(position: Int)
    }
    interface BtnMoreClickListener {
        fun onBtnMoreClick(position: Int, view: View)
    }
    fun setOnItemClickListener(listener: ItemClickListener){
        clickListener = listener
    }

    fun setOnBtnMoreClickListener(listener: BtnMoreClickListener){
        btnMoreClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemGroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false)
        return ItemGroupViewHolder(view,clickListener,btnMoreClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemGroupViewHolder
        val groupName = listGroup[position].name
        val avata = listGroup[position].avatar
        Glide.with(holder.itemView).load(avata).into(holder.iconGroup)
        holder.txtGroupName.text = groupName
        holder.btnMore.setOnClickListener {
            btnMoreClickListener.onBtnMoreClick(position,it)
//            it.tag = arrayOf(groupName, position)
//            it.parent.showContextMenuForChild(it)
        }
    }

    override fun getItemCount(): Int {
        return listGroup.size
    }
}