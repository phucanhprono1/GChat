package com.phucanh.gchat.ui.fragments.group

import android.content.Context
import android.content.Intent
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

class ListGroupAdapter(private val context: Context, private val listGroup: ArrayList<Group>,private val clickListener : ItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface ItemClickListener{
        fun onItemClick(group: Group)
    }
    inner class ItemGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnCreateContextMenuListener {
        var iconGroup: CircleImageView = itemView.findViewById(R.id.icon_group)
        var txtGroupName: TextView = itemView.findViewById(R.id.txtName)
        var btnMore: ImageButton = itemView.findViewById(R.id.btnMoreAction)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            view: View,
            contextMenuInfo: ContextMenu.ContextMenuInfo
        ) {
            menu.setHeaderTitle((btnMore.tag as Array<*>)[0] as String)
            val data = Intent()
            data.putExtra(
                GroupFragment.CONTEXT_MENU_KEY_INTENT_DATA_POS,
                (btnMore.tag as Array<*>)[1] as Int
            )
            menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_EDIT, Menu.NONE, "Edit group")
                .setIntent(data)
            menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_DELETE, Menu.NONE, "Delete group")
                .setIntent(data)
            menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_LEAVE, Menu.NONE, "Leave group")
                .setIntent(data)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemGroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false)
        return ItemGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemGroupViewHolder
        val groupName = listGroup[position].name
        val avata = listGroup[position].avatar
        Glide.with(holder.itemView).load(avata).into(holder.iconGroup)
        holder.txtGroupName.text = groupName
        holder.btnMore.setOnClickListener {
            it.tag = arrayOf(groupName, position)
            it.parent.showContextMenuForChild(it)
        }
    }

    override fun getItemCount(): Int {
        return listGroup.size
    }
}