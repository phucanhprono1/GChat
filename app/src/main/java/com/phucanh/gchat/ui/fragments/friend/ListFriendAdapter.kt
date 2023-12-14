package com.phucanh.gchat.ui.fragments.friend

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
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
import java.text.SimpleDateFormat
import java.util.Date

class ListFriendAdapter(
    private val context: Context,
    private var listFriend: ListFriend,
    val navController: NavController
) : RecyclerView.Adapter<ItemFriendViewHolder>() {

    companion object{
        var mapQuery = HashMap<String?, Query?>()
        var mapQueryOnline = HashMap<String?, DatabaseReference?>()
        var mapChildListener = HashMap<String?, ChildEventListener?>()
        var mapChildListenerOnline = HashMap<String?, ChildEventListener?>()
        var mapMark = HashMap<String?, Boolean?>()
        var mapMessage1 = HashMap<String?, String?>()
        var mapTimestamp = HashMap<String?, Long?>()
        var mapIsOnline = HashMap<String?, Boolean?>()
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemFriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false)
        return ItemFriendViewHolder(context, view, mListener, mLongListener)
    }

    override fun onBindViewHolder(holder: ItemFriendViewHolder, position: Int) {


        if (listFriend.listFriend.get(position) != null) {
            holder.itemView.setOnClickListener {
                mListener.onClick(position)
                holder.txtName.typeface = Typeface.DEFAULT
                holder.txtMessage.typeface = Typeface.DEFAULT
                holder.txtTime.typeface = Typeface.DEFAULT
            }
            val name = listFriend.listFriend?.get(position)!!.user.name
            val id: String = listFriend.listFriend?.get(position)!!.id
            val idRoom = listFriend.listFriend?.get(position)!!.idRoom
            val avata = listFriend.listFriend?.get(position)!!.user.avata

            holder.txtName.text = name
            mapMark[id]=null
            val connected = ServiceUtils.isNetworkConnected(holder.context)
            if (mapQuery[id] == null && mapChildListener[id] == null) {

                mapQuery[id] =
                    FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .reference.child("message")
                        .child(idRoom)
                        .limitToLast(1)
                Log.d("ListFriendAdapter","$id map mark: ${mapMark[id]}")
                var childEventListener = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val mapMessage = dataSnapshot.value as HashMap<*, *>?
                        Log.d("ListFriendAdapter","${mapMessage?.get("content")}")

//                            listFriendItem.user.message?.content = "$id${mapMessage!!["content"]}"
                        if (mapMark[id] != null) {

                            if (!mapMark[id]!!) {

                                // Update message content
                                mapMessage1[id] = "$id${mapMessage?.get("content")}"
                                Log.d("ListFriendsAdapter", "Message Content: $id ${mapMessage1[id]}")

                                // Notify the adapter on the UI thread
                                notifyDataSetChanged()
                            } else {
                                mapMessage1[id] = mapMessage?.get("content") as String
                                Log.d("ListFriendsAdapter", "Message Content 1: ${mapMessage1[id]}")
                            }
                        } else {
                            listFriend.listFriend?.get(position)!!!!.user.message?.content = mapMessage?.get("content") as String
                            Log.d("ListFriendsAdapter", "Message Content 2: $id ${listFriend.listFriend?.get(position)!!.user.message?.content}")
                        }
                        mapTimestamp[id] = mapMessage?.get("timestamp") as Long
                    }


                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                mapChildListener[id] = childEventListener
//                    mapQuery[id]?.addChildEventListener(childEventListener)
                mapChildListener[id]?.let { mapQuery[id]?.addChildEventListener(it) }
                Log.d("FriendsAdapter", "mapQuery[$id]: ${mapQuery[id]}")
                mapMark[id] = true
            }
            else {
                mapChildListener[id]?.let { mapQuery[id]?.removeEventListener(it) }
                mapChildListener[id]?.let { mapQuery[id]?.addChildEventListener(it) }
                mapMark[id] = true
            }
            if (!mapMessage1[id].isNullOrEmpty() && connected) {
                Log.d("not null", "not nul")
                holder.txtMessage.visibility = View.VISIBLE
                holder.txtTime.visibility = View.VISIBLE
                if (!mapMessage1[id]?.startsWith(id)!!) {
                    holder.txtMessage.text = mapMessage1[id]
                    holder.txtMessage.typeface = Typeface.DEFAULT
                    holder.txtName.typeface = Typeface.DEFAULT
                } else {
                    holder.txtMessage.text = mapMessage1[id]?.substring((id+"").length)
                    holder.txtMessage.typeface = Typeface.DEFAULT_BOLD
                    holder.txtName.typeface = Typeface.DEFAULT_BOLD
                }
                var timestamp = mapTimestamp[id]
                var time =
                    SimpleDateFormat("EEE, d MMM yyyy").format(timestamp?.let { Date(it) })
                var today =
                    SimpleDateFormat("EEE, d MMM yyyy").format(Date(System.currentTimeMillis()))
                if (today == time) {
                    holder.txtTime.text =
                        SimpleDateFormat("HH:mm").format(timestamp?.let { Date(it) })
                } else {
                    holder.txtTime.text =
                        SimpleDateFormat("MMM d").format(timestamp?.let { Date(it) })
                }
            } else {
                holder.txtMessage.visibility = View.GONE
                holder.txtTime.visibility = View.GONE
                Log.d("null", "null")


            }

            Glide.with(holder.itemView).load(Uri.parse(avata)).override(300, 300)
                .into(holder.avata)


            if (mapQueryOnline[id] == null && mapChildListenerOnline[id] == null && connected) {
                mapQueryOnline[id] = FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users")
                    .child(id)
                    .child("status")

                mapChildListenerOnline[id] = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {

                            if (mapIsOnline[id] == null ) {
                                mapIsOnline[id] = dataSnapshot.getValue(Boolean::class.java) ?: false
                                Log.d("FriendsAdapter", "Is Online: ${mapIsOnline[id]}")

                                notifyDataSetChanged()
                            }

                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {
                            if (mapIsOnline[id] == null ) {
                                mapIsOnline[id] = dataSnapshot.getValue(Boolean::class.java) ?: false
                                Log.d("FriendsAdapter", "Is Online: ${mapIsOnline[id]}")

                                notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                mapChildListenerOnline[id]?.let { mapQueryOnline[id]?.addChildEventListener(it) }
            }

            if (mapIsOnline[id] == true) {
                holder.avata.borderWidth = 10
            } else {
                holder.avata.borderWidth = 0

            }
            Log.d("FriendsAdapter", "Friend ID: $id")
            Log.d("FriendsAdapter", "Friend Name: $name")
            Log.d("FriendsAdapter", "Friend Avata: $avata")
            Log.d("FriendsAdapter", "Friend ID Room: $idRoom")
        }
    }

    override fun getItemCount(): Int {
        return listFriend.listFriend?.size ?: 0
    }


}