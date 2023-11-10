package com.phucanh.gchat.viewModels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.FriendRequest
import com.phucanh.gchat.models.User
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(val userReference: DatabaseReference,val firebaseDatabase: FirebaseDatabase,application:Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    val chosenuser : MutableLiveData<User> = MutableLiveData()
//    init {
//        checkFriendRequest(StaticConfig.UID)
//    }
    fun getCurrentUser(uid:String){
        userReference.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    chosenuser.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }
    fun sendPushNotification(receiverUserId: String, notificationMessage: String) {
        Log.d(
            "FCM",
            "Preparing to send notification with token: $receiverUserId $notificationMessage"
        )
        Log.d("FCM", "Preparing to send notification")
        // Lấy FCM token của người nhận tin nhắn

        userReference.child(receiverUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val receiverUser = dataSnapshot.getValue(User::class.java)
                    val fcmToken: String = receiverUser?.fcmToken!!

                    // Gửi thông báo đẩy bằng FCM
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        Log.d("FCM", "Sending notification$fcmToken")
                        sendNotificationUsingFCM(fcmToken, notificationMessage)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    fun sendNotificationUsingFCM(fcmToken: String, notificationMessage: String) {
        // Gửi thông báo đẩy bằng FCM
        Log.d(
            "FCM",
            "Preparing to send notification with token: $fcmToken $notificationMessage"
        )
        try {
//            JSONObject notification = new JSONObject();
//            notification.put("title", StaticConfig.NAME); // Tiêu đề thông báo
//            notification.put("body", notificationMessage); // Nội dung thông báo
//
//            JSONObject message = new JSONObject();
//            message.put("token", fcmToken); // FCM token của người nhận
//            message.put("notification", notification);
//            JSONObject sending = new JSONObject();
//            sending.put("message", message);
            val jsonObject = JSONObject()
            val notificationObj = JSONObject()
            notificationObj.put("title", StaticConfig.NAME)
            notificationObj.put("body", notificationMessage)
            val dataObj = JSONObject()
            jsonObject.put("notification", notificationObj)
            jsonObject.put("to", fcmToken)
            // Tạo kết nối HTTP để gửi thông báo đẩy
            val client = OkHttpClient()
            val JSON: MediaType = MediaType.get("application/json; charset=utf-8")
            Log.d(
                "FCM",
                "Preparing to send notification with token: $fcmToken $jsonObject"
            )
            //RequestBody body = RequestBody.create(JSON,sending.toString());
//            Request request = new Request.Builder()
//                    .url("https://fcm.googleapis.com/v1/projects//messages:send")
//                    .post(body)
//                    .addHeader("Authorization", "Bearer ya29.a0AfB_byDZuHXk5Ow5QN2SfBGUr2dIdS8cUbPEcVAuo3aMV9GPI40r8IoXNTXDJ74C8AMLZJrx1R3xLPuRQIsl8i-kPH_XnFzHpwUXCOyT23ESgD86JDy3LdhmYRZxjHGxef3YqrWVIzOhMrAM3snULoyLuGeEBBDyr88B5gaCgYKAQsSARASFQHsvYlsowK03wNAsul0lnBu_GHJ3w0173") // Thay YOUR_SERVER_KEY bằng server key của bạn từ Firebase Console
//                    .build();
            val body: RequestBody = RequestBody.create(JSON, jsonObject.toString())
            val request= Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader(
                    "Authorization",
                    "Bearer AAAAYNK5K38:APA91bGBSn8zGVEymeT_8nrGKMhpaGIx7TXhnncY0KZVvhewEDFpYEqgYY6lwNF-kJ5Q1grftUXJSEfCcIg4uL4NajN8KkqK9cAGid7q1Gr7zoLYS1QbJRu5xDFGgA7RgW4arKZJLIaH"
                ) // Thay YOUR_SERVER_KEY bằng server key của bạn từ Firebase Console
                .build()
            // Thực hiện request để gửi thông báo đẩy
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    // Handle error
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    Log.d("FCM", response.body()!!.string())
                }
            }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun checkFriendRequest(idFriend: String?):Boolean{
        var isFriend = false
        if (idFriend != null) {
            val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
            val friendRequestRef = firebaseDatabase.getReference("friend_requests")
            friendRequestRef.child(currentUserID).child(idFriend).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val friendRequest = snapshot.getValue(FriendRequest::class.java)
                        if (friendRequest?.status.equals("pending")) {
                            isFriend = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }
        return isFriend
    }
    fun addFriendRequest(idFriend: String?) {
        if (idFriend != null ) {
            val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
            var friendRequest = FriendRequest()
            friendRequest.idReceiver = idFriend
            friendRequest.idSender = currentUserID
            friendRequest.status = "pending"
            friendRequest.nameSender = StaticConfig.NAME
            friendRequest.imageSender = StaticConfig.AVATA
            friendRequest.createdAt = System.currentTimeMillis().toString()
            friendRequest.idRoom= if (idFriend.compareTo(StaticConfig.UID) > 0) (StaticConfig.UID + idFriend).hashCode()
                    .toString() + "" else "" + (idFriend + StaticConfig.UID).hashCode();
            val friendRequestRef = firebaseDatabase.getReference("friend_requests")
            friendRequestRef.child(idFriend).child(currentUserID).setValue(friendRequest)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(getApplication(), "Friend request sent", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "Failed to send friend request",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        getApplication(),
                        "Failed to send friend request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}