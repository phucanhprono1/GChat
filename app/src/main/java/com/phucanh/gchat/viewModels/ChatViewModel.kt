package com.phucanh.gchat.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phucanh.gchat.models.Conversation
import com.phucanh.gchat.models.Message
import com.phucanh.gchat.models.User
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(val userReference: DatabaseReference,val firebaseDatabase: FirebaseDatabase,application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    var mapAvatar = mutableMapOf<String,String>()
    var avatar = ""
    var listMessage = MutableLiveData<Conversation>()
    var friendids = ArrayList<CharSequence>()
    var conversation = Conversation()
    var imgUri:Uri? = null
    var imgUriLink = ""
    var roomId: String = ""
    var roomName: String = ""
    var isChat:Boolean? = false
    var isGroupChat:Boolean? = false

    fun getListMessage(idRoom: String) {

            val conversation = listMessage.value ?: Conversation()

            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null && !conversation.listMessageData.contains(message)) {
                        conversation.listMessageData.add(message)
                        listMessage.value = conversation
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle message change if needed
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle message removal if needed
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle message movement if needed
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }

            // Remove the existing child event listener before adding a new one
            firebaseDatabase.reference.child("message").child(idRoom).removeEventListener(childEventListener)

            // Add the new child event listener
            firebaseDatabase.reference.child("message").child(idRoom).addChildEventListener(childEventListener)


    }
    fun sendMessage(content: String, idRoom: String, type: Int, receiverId: ArrayList<CharSequence>,fileName:String?){
        val message: Message = Message()
        message.content = content
        message.idSender = FirebaseAuth.getInstance().currentUser?.uid.toString()
        message.type = type
        message.timestamp = System.currentTimeMillis()
        message.nameSender = StaticConfig.NAME
        message.idReceiver = idRoom
        firebaseDatabase.reference.child("message").child(idRoom).push().setValue(message)
        if(type == 0){
            for ( id in receiverId){
                if(id.toString() == StaticConfig.UID){
                    continue
                }
                sendPushNotification(id.toString(),content)
            }
        }
        else if(type == 1){
            for ( id in receiverId){
                if(id.toString() == StaticConfig.UID){
                    continue
                }
                sendPushNotification(id.toString(),"Sent an image")
            }
        }
        else if(type == 2){
            for ( id in receiverId){
                if(id.toString() == StaticConfig.UID){
                    continue
                }
                sendPushNotification(id.toString(),"Sent a video clip")
            }
        }
    }
    fun sendPushNotification(receiverUserId: String, notificationMessage: String) {
        Log.d(
            "FCM",
            "Preparing to send notification with token: $receiverUserId $notificationMessage"
        )
        Log.d("FCM", "Preparing to send notification")

        userReference.child(receiverUserId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val receiverUser = dataSnapshot.getValue(User::class.java)
                    if (receiverUser != null) {
                        if (receiverUser.fcmToken !=null){
                            val fcmToken: String = receiverUser?.fcmToken!!

                            // Gửi thông báo đẩy bằng FCM
                            if (fcmToken != null && !fcmToken.isEmpty() && fcmToken != StaticConfig.FCMTOKEN) {
                                Log.d("FCM", "Sending notification$fcmToken")
                                sendNotificationUsingFCM(fcmToken, notificationMessage)
                            }
                        }
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
            val jsonObject = JSONObject()
            val notificationObj = JSONObject()
            notificationObj.put("title", roomName)
            val dataObj1 = JSONObject()
            dataObj1.put("roomId", roomId)
            dataObj1.put("friendids", friendids)
            dataObj1.put("name", roomName)
            dataObj1.put("avatar", avatar)
            dataObj1.put("message", notificationMessage)
            dataObj1.put("isChat", true)
            dataObj1.put("isGroupChat", isGroupChat)
            notificationObj.put("body", dataObj1)

            jsonObject.put("notification", notificationObj)
            jsonObject.put("to", fcmToken)
            // Tạo kết nối HTTP để gửi thông báo đẩy
            val client = OkHttpClient()
            //val JSON: MediaType = MediaType.get("application/json; charset=utf-8")
            val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
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


                override fun onFailure(call: Call, e: IOException) {
                    Log.d("FCM", "onFailure: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("FCM", "onResponse: $response")
                }
            }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}