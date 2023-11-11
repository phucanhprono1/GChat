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
import com.google.firebase.database.ktx.getValue
import com.phucanh.gchat.R
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.models.Message

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
                txtName.typeface = Typeface.DEFAULT
                txtMessage.typeface = Typeface.DEFAULT
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
        var mapQuery = HashMap<String?, Query?>()
        var mapQueryOnline = HashMap<String?, DatabaseReference?>()
        var mapChildListener = HashMap<String?, ChildEventListener?>()
        var mapChildListenerOnline = HashMap<String?, ChildEventListener>()
        var mapMark = HashMap<String?, Boolean?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false)
        return ItemFriendViewHolder(context, view, mListener, mLongListener)
    }

    override fun onBindViewHolder(holder: ItemFriendViewHolder, position: Int) {
        val listFriendItem = listFriend.listFriend?.get(position)

            val name = listFriend.listFriend!![position]!!.user.name
            val id: String = listFriend.listFriend!![position]!!.id
            val idRoom = listFriend.listFriend!![position]!!.idRoom
            val avata = listFriend.listFriend!![position]!!.user.avata

            holder.txtName.text = name
            val connected = ServiceUtils.isNetworkConnected(holder.context)

            if (listFriend.listFriend!![position]!!.user.message != null && listFriend.listFriend!![position]!!.user.message!!.content != null && connected) {
                holder.txtMessage.visibility = View.VISIBLE
                holder.txtTime.visibility = View.VISIBLE
                if (listFriend.listFriend!![position]!!.user.message!!.content!!.startsWith(id)) {
                    holder.txtMessage.text = listFriend.listFriend!![position]!!.user.message!!.content
                    holder.txtMessage.typeface = Typeface.DEFAULT
                    holder.txtName.typeface = Typeface.DEFAULT
                } else {
                    holder.txtMessage.text =
                        listFriend.listFriend!![position]!!.user.message!!.content!!.substring((id + "").length)
                    holder.txtMessage.typeface = Typeface.DEFAULT_BOLD
                    holder.txtName.typeface = Typeface.DEFAULT_BOLD
                }

                var time =
                    SimpleDateFormat("EEE, d MMM yyyy").format(Date(listFriend.listFriend!![position]!!.user.message!!.timestamp))
                val today =
                    SimpleDateFormat("EEE, d MMM yyyy").format(Date(System.currentTimeMillis()))
                if (today == time) {
                    holder.txtTime.text =
                        SimpleDateFormat("HH:mm").format(Date(listFriend.listFriend!![position]!!.user.message!!.timestamp))
                } else {
                    holder.txtTime.text =
                        SimpleDateFormat("MMM d").format(Date(listFriend.listFriend!![position]!!.user.message!!.timestamp))
                }
            } else {
                holder.txtMessage.visibility = View.GONE
                holder.txtTime.visibility = View.GONE
                if (mapQuery[id] == null && mapChildListener[id] == null) {
                    mapQuery[id] =
                        FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("message")
                            .child(idRoom)
                            .limitToLast(1)

                    mapChildListener[id] = object : ChildEventListener {
                        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                            val mapMessage = dataSnapshot.value as HashMap<*, *>?
                            if (mapMark[id] != null) {
                                if (!mapMark[id]!!) {
                                    listFriend.listFriend!![position]!!.user.message?.content =
                                        "$id${mapMessage!!["content"]}"
                                    Log.d("FriendsAdapter", "Message Content: ${listFriend.listFriend!![position]!!.user.message?.content}")

                                } else {
                                    listFriend.listFriend!![position]!!.user.message?.content =
                                        mapMessage!!["content"] as String
                                }
                                notifyDataSetChanged()
                                mapMark[id] = false
                            } else {
                                listFriend.listFriend!![position]!!.user.message?.content =
                                    mapMessage!!["content"] as String
                                notifyDataSetChanged()
                            }
                            listFriend.listFriend!![position]!!.user.message?.timestamp = mapMessage["timestamp"] as Long
                        }

                        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                        override fun onCancelled(databaseError: DatabaseError) {}
                    }

                    mapQuery[id]?.addChildEventListener(mapChildListener[id]!!)
                    mapMark[id] = true
                } else {
                    mapQuery[id]?.removeEventListener(mapChildListener[id]!!)
                    mapQuery[id]?.addChildEventListener(mapChildListener[id]!!)
                    mapMark[id] = true
                }
            }

            Glide.with(holder.itemView).load(Uri.parse(avata))
                .into(holder.avata)


            if (mapQueryOnline[id] == null && mapChildListenerOnline[id] == null && connected) {
                mapQueryOnline[id] = FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(id)
                    .child("status")

                mapChildListenerOnline[id] = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {

                            if (listFriend.listFriend!![position]!!.user != null && listFriend.listFriend!![position]!!.user.status != null) {
                                listFriend.listFriend!![position]!!.user.status?.isOnline = dataSnapshot.getValue(Boolean::class.java) ?: false

                                notifyDataSetChanged()
                            }

                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {


                            if (listFriend.listFriend!![position]!!.user != null && listFriend.listFriend!![position]!!.user.status != null) {
                                listFriend.listFriend!![position]!!.user.status?.isOnline = dataSnapshot.getValue(Boolean::class.java) ?: false

                                notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                    override fun onCancelled(databaseError: DatabaseError) {}
                }

                mapQueryOnline[id]?.addChildEventListener(mapChildListenerOnline[id]!!)
            }

            if (listFriend.listFriend!![position]!!.user.status?.isOnline == true) {
                holder.avata.borderWidth = 10
            } else {
                holder.avata.borderWidth = 0

            }
            Log.d("FriendsAdapter", "Friend ID: $id")
            Log.d("FriendsAdapter", "Friend Name: $name")
            Log.d("FriendsAdapter", "Friend Avata: $avata")
            Log.d("FriendsAdapter", "Friend ID Room: $idRoom")

// Check if mapQuery and mapChildListener have the correct values
            Log.d("FriendsAdapter", "mapQuery[$id]: ${mapQuery[id]}")
            Log.d("FriendsAdapter", "mapChildListener[$id]: ${mapChildListener[id]}")

// Check if mapQueryOnline and mapChildListenerOnline have the correct values
            Log.d("FriendsAdapter", "mapQueryOnline[$id]: ${mapQueryOnline[id]}")
            Log.d("FriendsAdapter", "mapChildListenerOnline[$id]: ${mapChildListenerOnline[id]}")

// Check the values you are trying to retrieve
//            Log.d("FriendsAdapter", "Message Content: ${listFriend.listFriend!![position]!!.user.message?.content}")
//            Log.d("FriendsAdapter", "Message Timestamp: ${listFriend.listFriend!![position]!!.user.message?.timestamp}")
//            Log.d("FriendsAdapter", "Is Online: ${listFriend.listFriend!![position]!!.user.status?.isOnline}")

    }

    override fun getItemCount(): Int {
        return listFriend.listFriend?.size ?: 0
    }


}