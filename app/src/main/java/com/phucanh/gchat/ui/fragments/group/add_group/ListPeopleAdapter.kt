package com.phucanh.gchat.ui.fragments.group.add_group

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.phucanh.gchat.R
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.utils.StaticConfig
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashSet

class ListPeopleAdapter(
    private val context: Context,
    private var listFriend: ListFriend?,
    private val btnAddGroup: LinearLayout,
    private var listIDChoose: MutableSet<String>,
    private var listIDRemove: MutableSet<String>,
    private var isEdit: Boolean?,
    private var editGroup: Group?,
    private val friendSelectionListener: FriendSelectionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface FriendSelectionListener {
        fun onFriendSelected(id: String)
        fun onFriendDeselected(id: String)
    }

    inner class ItemFriendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtName: TextView
        var txtEmail: TextView
        var avata: CircleImageView
        var checkBox: CheckBox

        init {
            txtName = itemView.findViewById<View>(R.id.txtName) as TextView
            txtEmail = itemView.findViewById<View>(R.id.txtEmail) as TextView
            avata = itemView.findViewById<View>(R.id.icon_avata) as CircleImageView
            checkBox = itemView.findViewById<View>(R.id.checkAddPeople) as CheckBox
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_add_friend, parent, false)
        return ItemFriendHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val friendHolder = holder as ItemFriendHolder
        friendHolder.txtName.text = listFriend!!.listFriend!![position]!!.user.name
        friendHolder.txtEmail.text = listFriend!!.listFriend!![position]!!.user.email
        val avata = listFriend!!.listFriend!![position]!!.user.avata
        val id = listFriend!!.listFriend!![position]!!.id

        if (avata != StaticConfig.STR_DEFAULT_URI) {
            Glide.with(holder.itemView).load(avata).apply(RequestOptions.circleCropTransform()).into(friendHolder.avata)
        } else {
            friendHolder.avata.setImageBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.default_avata
                )
            )
        }

        friendHolder.checkBox.setOnCheckedChangeListener { _, b ->
            if (b) {
//                listIDChoose.add(id)
//                listIDRemove.remove(id)
                friendSelectionListener.onFriendSelected(id)
            } else {
//                listIDRemove.add(id)
//                listIDChoose.remove(id)
                friendSelectionListener.onFriendDeselected(id)
            }

            btnAddGroup.setBackgroundColor(
                if (listIDChoose.size >= 3) {
                    context.resources.getColor(R.color.colorPrimary)
                } else {
                    context.resources.getColor(R.color.grey_500)
                }
            )
        }

        if (isEdit ==true && editGroup?.members?.contains(id) == true) {
            friendHolder.checkBox.isChecked = true
        } else if (isEdit ==false && editGroup != null && !editGroup!!.members!!.contains(id)) {
            friendHolder.checkBox.isChecked = false
        }
    }

    override fun getItemCount(): Int {
        return listFriend?.listFriend?.size ?: 0
    }
}
