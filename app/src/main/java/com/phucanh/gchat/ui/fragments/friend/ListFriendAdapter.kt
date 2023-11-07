package com.phucanh.gchat.ui.fragments.friend

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.phucanh.gchat.R
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date

class ListFriendsAdapter(
    private val context: Context,
    private val listFriend: ListFriend,
    val fragment: FriendFragment,
    val navController: NavController
) : RecyclerView.Adapter<ListFriendsAdapter.ItemFriendViewHolder>() {
    fun removeItem(position: Int) {
        listFriend.listFriend?.removeAt(position)
        notifyItemRemoved(position)
    }
    fun updateData(listFriend: ListFriend) {
        this.listFriend.listFriend?.toMutableList()?.clear()
        this.listFriend.listFriend?.toMutableList()?.addAll(listFriend.listFriend!!)
        notifyDataSetChanged()
    }
    inner class ItemFriendViewHolder(
        context: Context,
        view: View,
        listener: OnClickListener,
        longListener: OnLongClickListener
    ) : RecyclerView.ViewHolder(view) {
        val avata: CircleImageView = itemView.findViewById(R.id.icon_avata)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        val context: Context = context

        init {
            itemView.setOnClickListener {
                listener.onClick(bindingAdapterPosition)
            }
            itemView.setOnLongClickListener {
                longListener.onLongClick(it)
            }
        }
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    private lateinit var mListener: OnClickListener
    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnLongClickListener {
        fun onLongClick(view: View): Boolean
    }

    private lateinit var mLongListener: OnLongClickListener
    fun setOnLongClickListener(listener: OnLongClickListener) {
        mLongListener = listener
    }

    companion object {
        var mapQuery = mutableMapOf<String?, Query>()
        var mapQueryOnline = mutableMapOf<String?, DatabaseReference>()
        var mapChildListener = mutableMapOf<String?, ChildEventListener>()
        var mapChildListenerOnline = mutableMapOf<String?, ChildEventListener>()
        var mapMark = mutableMapOf<String?, Boolean>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false)
        return ItemFriendViewHolder(context, view, mListener, mLongListener)
    }

    override fun onBindViewHolder(holder: ItemFriendViewHolder, position: Int) {
        val listFriendItem = listFriend.listFriend?.get(position)
        if (listFriendItem != null) {
            val name = listFriendItem.user.name
            val id: String = listFriendItem.id
            val idRoom = listFriendItem.idRoom
            val avata = listFriendItem.user.avata

            holder.txtName.text = name
            val connected = ServiceUtils.isNetworkConnected(holder.context)

//            holder.itemView.setOnClickListener {
//                holder.txtMessage.typeface = Typeface.DEFAULT
//                holder.txtName.typeface = Typeface.DEFAULT
//            }

            if (listFriendItem.user.message != null && listFriendItem.user.message!!.content != null && connected) {
                holder.txtMessage.visibility = View.VISIBLE
                holder.txtTime.visibility = View.VISIBLE
                if (listFriendItem.user.message!!.content!!.startsWith(id!!)) {
                    holder.txtMessage.text = listFriendItem.user.message!!.content
                    holder.txtMessage.typeface = Typeface.DEFAULT
                    holder.txtName.typeface = Typeface.DEFAULT
                } else {
                    holder.txtMessage.text =
                        listFriendItem.user.message!!.content!!.substring((id + "").length)
                    holder.txtMessage.typeface = Typeface.DEFAULT_BOLD
                    holder.txtName.typeface = Typeface.DEFAULT_BOLD
                }

                val time =
                    SimpleDateFormat("EEE, d MMM yyyy").format(Date(listFriendItem.user.message!!.timestamp))
                val today =
                    SimpleDateFormat("EEE, d MMM yyyy").format(Date(System.currentTimeMillis()))
                if (today == time) {
                    holder.txtTime.text =
                        SimpleDateFormat("HH:mm").format(Date(listFriendItem.user.message!!.timestamp))
                } else {
                    holder.txtTime.text =
                        SimpleDateFormat("MMM d").format(Date(listFriendItem.user.message!!.timestamp))
                }
            } else {
                holder.txtMessage.visibility = View.GONE
                holder.txtTime.visibility = View.GONE
                if (mapQuery[id] == null && mapChildListener[id] == null) {
                    mapQuery[id] to
                        FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
                            .getReference("message")
                            .child(idRoom)
                            .limitToLast(1)

                    mapChildListener[id] to object : ChildEventListener {
                        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                            val mapMessage = dataSnapshot.value as HashMap<*, *>
                            if (mapMark[id] != null) {
                                if (!mapMark[id]!!) {
                                    listFriendItem.user.message?.content =
                                        "$id${mapMessage["content"]}"
                                } else {
                                    listFriendItem.user.message?.content =
                                        mapMessage["content"] as String
                                }
                                notifyDataSetChanged()
                                mapMark[id] = false
                            } else {
                                listFriendItem.user.message?.content =
                                    mapMessage["content"] as String
                                notifyDataSetChanged()
                            }
                            listFriendItem.user.message?.timestamp = mapMessage["timestamp"] as Long
                        }

                        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onCancelled(databaseError: DatabaseError) {}
                    }

                    mapQuery[id]?.addChildEventListener(mapChildListener[id]!!)
                    mapMark[id] to true
                } else {
                    mapQuery[id]?.removeEventListener(mapChildListener[id]!!)
                    mapQuery[id]?.addChildEventListener(mapChildListener[id]!!)
                    mapMark[id] to true
                }
            }

            if (listFriendItem.user.avata == StaticConfig.AVATA) {
                Glide.with(holder.itemView).load(StaticConfig.AVATA).into(holder.avata)
            } else {
                Glide.with(holder.itemView).load(Uri.parse(listFriendItem.user.avata))
                    .into(holder.avata)
            }

            if (mapQueryOnline[id] == null && mapChildListenerOnline[id] == null && connected) {
                mapQueryOnline[id] to
                    FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
                        .getReference("users")
                        .child(id!!)
                        .child("status")

                mapChildListenerOnline[id] to object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {
                            Log.d("FriendsFragment add $id", dataSnapshot.value.toString())
                            listFriendItem.user.status!!.isOnline = dataSnapshot.value as Boolean
                            notifyDataSetChanged()
                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {
                            Log.d("FriendsFragment change $id", dataSnapshot.value.toString())
                            listFriendItem.user.status!!.isOnline = dataSnapshot.value as Boolean
                            notifyDataSetChanged()
                        }
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                }

                mapQueryOnline[id]?.addChildEventListener(mapChildListenerOnline[id]!!)
            }

            if (listFriendItem.user.status?.isOnline == true) {
                holder.avata.borderWidth = 10
            } else {
                holder.avata.borderWidth = 0
            }
        }
    }

    override fun getItemCount(): Int {
        return listFriend.listFriend?.size ?: 0
    }


}