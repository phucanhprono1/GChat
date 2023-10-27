package com.phucanh.gchat.ui.fragments.friend

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
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
    private val fragment: FriendFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        var mapQuery = mutableMapOf<String?, Query>()
        var mapQueryOnline = mutableMapOf<String?, DatabaseReference>()
        var mapChildListener = mutableMapOf<String?, ChildEventListener>()
        var mapChildListenerOnline = mutableMapOf<String?, ChildEventListener>()
        var mapMark = mutableMapOf<String?, Boolean>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false)
        return ItemFriendViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val name = listFriend.listFriend!![position].name
        val id:String? = listFriend.listFriend!![position].id
        val idRoom = listFriend.listFriend!![position].idRoom
        val avata = listFriend.listFriend!![position].avata

        (holder as ItemFriendViewHolder).txtName.text = name
        val connected = ServiceUtils.isNetworkConnected(holder.context)

        holder.itemView.setOnClickListener {
            holder.txtMessage.setTypeface(Typeface.DEFAULT)
            holder.txtName.setTypeface(Typeface.DEFAULT)

        }

//        holder.itemView.setOnLongClickListener {
////            val friendName = holder.txtName.text.toString()
////            AlertDialog.Builder(context)
////                .setTitle("Choose Action")
////                .setMessage("Do you want to delete $friendName or view their wall?")
////                .setPositiveButton("Delete") { _, _ ->
////                    val idFriendRemoval = listFriend.listFriend!![position].id
////                    if (connected) {
////                        if (idFriendRemoval != null) {
////                            deleteFriend(idFriendRemoval)
////                        }
////                    }
////                }
////                .setNegativeButton("View Wall") { _, _ ->
////                    val intent = Intent(context, FriendWallActivity::class.java).apply {
////                        putExtra("friendId", listFriend.listFriend[position].id)
////                    }
////                    context.startActivity(intent)
////                }
////                .show()
////            true
//        }

        if (listFriend.listFriend!![position].message!!.text !=null && connected) {
            holder.txtMessage.visibility = View.VISIBLE
            holder.txtTime.visibility = View.VISIBLE
            if ( listFriend.listFriend!![position].message!!.text!!.startsWith(id!!)) {
                holder.txtMessage.text = listFriend.listFriend!![position].message!!.text
                holder.txtMessage.setTypeface(Typeface.DEFAULT)
                holder.txtName.setTypeface(Typeface.DEFAULT)
            } else {
                holder.txtMessage.text = listFriend.listFriend!![position].message!!.text!!.substring((id + "").length)
                holder.txtMessage.setTypeface(Typeface.DEFAULT_BOLD)
                holder.txtName.setTypeface(Typeface.DEFAULT_BOLD)
            }

            val time = SimpleDateFormat("EEE, d MMM yyyy").format(Date(listFriend.listFriend!![position].message!!.timestamp))
            val today = SimpleDateFormat("EEE, d MMM yyyy").format(Date(System.currentTimeMillis()))
            if (today == time) {
                holder.txtTime.text = SimpleDateFormat("HH:mm").format(Date(listFriend.listFriend!![position].message!!.timestamp))
            } else {
                holder.txtTime.text = SimpleDateFormat("MMM d").format(Date(listFriend.listFriend!![position].message!!.timestamp))
            }
        } else {
            holder.txtMessage.visibility = View.GONE
            holder.txtTime.visibility = View.GONE
            if (mapQuery[id] == null && mapChildListener[id] == null) {
                mapQuery[id] = FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
                    .getReference("message")
                    .child(idRoom)
                    .limitToLast(1)

                mapChildListener[id] = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val mapMessage = dataSnapshot.value as HashMap<*, *>
                        if (mapMark[id] != null) {
                            if (!mapMark[id]!!) {
                                listFriend.listFriend!![position].message!!.text = "$id${mapMessage["text"]}"
                            } else {
                                listFriend.listFriend!![position].message!!.text = mapMessage["text"] as String
                            }
                            notifyDataSetChanged()
                            mapMark[id] = false
                        } else {
                            listFriend.listFriend!![position].message!!.text = mapMessage["text"] as String
                            notifyDataSetChanged()
                        }
                        listFriend.listFriend!![position].message!!.timestamp = mapMessage["timestamp"] as Long
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

        if (listFriend.listFriend!![position].avata == StaticConfig.AVATA) {
            Glide.with(holder.itemView).load(StaticConfig.AVATA).into(holder.avata)
        } else {
            Glide.with(holder.itemView).load(Uri.parse(listFriend.listFriend!![position].avata)).into(holder.avata)
        }

        if (mapQueryOnline[id] == null && mapChildListenerOnline[id] == null && connected) {
            mapQueryOnline[id] = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(id!!)
                .child("status")

            mapChildListenerOnline[id] = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {
                        Log.d("FriendsFragment add $id", dataSnapshot.value.toString())
                        listFriend.listFriend!![position].status!!.isOnline = dataSnapshot.value as Boolean
                        notifyDataSetChanged()
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.value != null && dataSnapshot.key == "isOnline") {
                        Log.d("FriendsFragment change $id", dataSnapshot.value.toString())
                        listFriend.listFriend!![position].status!!.isOnline = dataSnapshot.value as Boolean
                        notifyDataSetChanged()
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            }

            mapQueryOnline[id]?.addChildEventListener(mapChildListenerOnline[id]!!)
        }

        if (listFriend.listFriend!![position].status!!.isOnline) {
            holder.avata.setBorderWidth(10)
        } else {
            holder.avata.setBorderWidth(0)
        }
    }

    override fun getItemCount(): Int {
        return listFriend.listFriend?.size ?: 0
    }

//    private fun deleteFriend(idFriend: String) {
//        if (idFriend != null ) {
//            FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
//                .getReference("friend")
//                .child(StaticConfig.UID)
//                .orderByValue()
//                .equalTo(idFriend)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.value == null) {
//                            // Friend not found
//                        } else {
//                            val idRemoval = (dataSnapshot.value as HashMap<*, *>).keys.iterator().next().toString()
//                            FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
//                                .getReference("friend")
//                                .child(StaticConfig.UID)
//                                .child(idRemoval)
//                                .removeValue()
//                                .addOnCompleteListener { task ->
//                                    if (task.isSuccessful) {
//                                        Toast.makeText(context, "Deleted friend", Toast.LENGTH_SHORT).show()
//                                        val intentDeleted = Intent(FriendFragment.ACTION_DELETE_FRIEND)
//                                        intentDeleted.putExtra("idFriend", idFriend)
//                                        context.sendBroadcast(intentDeleted)
//                                    }
//                                }
//                                .addOnFailureListener { e ->
//                                    Toast.makeText(context, "Error occurred during deleting friend", Toast.LENGTH_SHORT).show()
//                                }
//
//                            FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
//                                .getReference("friend_requests")
//                                .child(StaticConfig.UID)
//                                .child(idRemoval)
//                                .removeValue()
//
//                            FirebaseDatabase.getInstance(context.getString(R.string.firebase_database_url))
//                                .getReference("friend_requests")
//                                .child(idRemoval)
//                                .child(StaticConfig.UID)
//                                .removeValue()
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {}
//                })
//        } else {
//            Toast.makeText(context, "Error occurred during deleting friend", Toast.LENGTH_SHORT).show()
//        }
//    }

    inner class ItemFriendViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avata: CircleImageView = itemView.findViewById(R.id.icon_avata)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        val context: Context = context
    }
}
