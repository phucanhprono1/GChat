package com.phucanh.gchat.ui.fragments.friend_request

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phucanh.gchat.R
import com.phucanh.gchat.models.FriendRequest
import com.firebase.ui.database.FirebaseRecyclerAdapter

import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.phucanh.gchat.utils.ServiceUtils
import com.phucanh.gchat.utils.StaticConfig

class FriendRequestAdapter(
    options: FirebaseRecyclerOptions<FriendRequest>,
    private val acceptClickListener: AcceptClickListener,
    private val deleteClickListener: DeleteClickListener
) : FirebaseRecyclerAdapter<FriendRequest, FriendRequestAdapter.ViewHolder>(options) {

    interface AcceptClickListener {
        fun onAcceptClick(friendRequest: FriendRequest)
    }

    interface DeleteClickListener {
        fun onDeleteClick(friendRequest: FriendRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        Log.d("FriendRequestAdapter", "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, friendRequest: FriendRequest) {
        Glide.with(holder.itemView).load(friendRequest.imageSender).into(holder.imageViewProfile)
        Log.d("FriendRequestAdapter", "onBindViewHolder for position $position")
        holder.textViewName.text = friendRequest.nameSender

        val connected = ServiceUtils.isNetworkConnected(holder.itemView.context)

        holder.buttonAccept.setOnClickListener {
            if (connected && acceptClickListener != null) {
                acceptClickListener.onAcceptClick(friendRequest)
//
//                friendRequestRef.child(StaticConfig.UID).child(friendRequest.idSender).removeValue()

                holder.buttonAccept.visibility = View.GONE
                holder.buttonDelete.visibility = View.GONE
                holder.notification.visibility = View.VISIBLE
                holder.notification.text = "Friend Request Accepted"
            }
        }

        holder.buttonDelete.setOnClickListener {
            if (connected && deleteClickListener != null) {
                deleteClickListener.onDeleteClick(friendRequest)

//                friendRequestRef.child(StaticConfig.UID).child(friendRequest.idSender).removeValue()

                holder.buttonAccept.visibility = View.GONE
                holder.buttonDelete.visibility = View.GONE
                holder.notification.visibility = View.VISIBLE
                holder.notification.text = "Friend Request Rejected"
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProfile = itemView.findViewById<ImageView>(R.id.imageView_profile)
        val textViewName = itemView.findViewById<TextView>(R.id.textView_name)
        val textViewMessage = itemView.findViewById<TextView>(R.id.textView_message)
        val buttonAccept = itemView.findViewById<Button>(R.id.button_accept)
        val buttonDelete = itemView.findViewById<Button>(R.id.button_delete)
        val notification = itemView.findViewById<TextView>(R.id.notification_background)
    }

}