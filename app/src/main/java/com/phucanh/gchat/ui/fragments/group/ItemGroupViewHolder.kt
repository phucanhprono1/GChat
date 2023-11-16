package com.phucanh.gchat.ui.fragments.group

import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.phucanh.gchat.R
import de.hdodenhof.circleimageview.CircleImageView

class ItemGroupViewHolder(itemView: View,listener: ListGroupAdapter.ItemClickListener,moreClickListener: ListGroupAdapter.BtnMoreClickListener) : RecyclerView.ViewHolder(itemView){
    var iconGroup: CircleImageView = itemView.findViewById(R.id.icon_group)
    var txtGroupName: TextView = itemView.findViewById(R.id.txtName)
    var btnMore: ImageButton = itemView.findViewById(R.id.btnMoreAction)


    init {

        itemView.setOnClickListener {
            listener.onItemClick(bindingAdapterPosition)
        }
        btnMore.setOnClickListener {
            Log.d("ItemGroupViewHolder", "onCreateContextMenu: ${txtGroupName.text}")
            moreClickListener.onBtnMoreClick(absoluteAdapterPosition,it)

        }
    }

}