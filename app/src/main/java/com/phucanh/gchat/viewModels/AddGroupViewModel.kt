package com.phucanh.gchat.viewModels

import android.app.Activity.RESULT_OK
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.phucanh.gchat.models.Friend
import com.phucanh.gchat.models.Group
import com.phucanh.gchat.models.GroupMember
import com.phucanh.gchat.models.ListFriend
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.room.GroupDao
import com.phucanh.gchat.ui.fragments.group.add_group.AddGroupFragment
import com.phucanh.gchat.utils.StaticConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.HashMap
import javax.inject.Inject
@HiltViewModel
class AddGroupViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val friendDao: FriendDao
    ,private val groupDao: GroupDao,
    application: Application)
    : AndroidViewModel(application) {
    var listIDChoose = HashSet<String>()
    var listIDRemove = HashSet<String>()
    var listFriend : ListFriend = ListFriend()
    var isEditGroup = false
    var group: Group? = null
    var avatarGroup: String? = null
    var nameGroup: String? = null
    var idGroup: String? = null
    var avatarGroupUri: Uri? = null
    val _listFriend: MutableLiveData<ListFriend?> = MutableLiveData()
    init {
        _listFriend.value = listFriend
        getListFriend()
    }
    fun getListFriend(){
        listFriend!!.listFriend = friendDao.getAll() as ArrayList<Friend?>
        _listFriend.value = listFriend
    }
    fun createGroup(){

        val idGroupCreate = (StaticConfig.UID + System.currentTimeMillis()).hashCode().toString()
        val groupCreate = Group()
        for(id in listIDChoose){
            groupCreate.members.add(id)
        }
        groupCreate.id = idGroupCreate
        groupCreate.admin = StaticConfig.UID
        groupCreate.name = nameGroup
        groupCreate.avatar = avatarGroup
        firebaseDatabase.reference.child("group").child(idGroupCreate).setValue(groupCreate).addOnCompleteListener {
            addRoomForUser(idGroupCreate, 0)
        }
            .addOnFailureListener() {
                Toast.makeText(getApplication(), "Create group failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteRoomForUser(roomId: String, userIndex: Int) {
        if (userIndex == listIDRemove.size) {
            return
        } else {
            firebaseDatabase.reference
                .child("users/${listIDRemove.toMutableList()[userIndex]}/group/$roomId")
                .removeValue()
                .addOnCompleteListener { task ->
                    deleteRoomForUser(roomId, userIndex + 1)
                }
                .addOnFailureListener { e ->
                    // Handle failure if needed
                }
        }
    }
    fun addRoomForUser(roomId: String, userIndex: Int) {
        if (userIndex == listIDChoose.size) {
            if (!isEditGroup) {
                return
            } else {
                deleteRoomForUser(roomId, 0)
            }
        } else {
            firebaseDatabase.reference
                .child("users/${listIDChoose.toMutableList()[userIndex]}/group/$roomId")
                .setValue(roomId)
                .addOnCompleteListener { task ->
                    addRoomForUser(roomId, userIndex + 1)
                }
                .addOnFailureListener { e ->
                    // Handle failure if needed
                }
        }
    }
    fun getGroup(idGroup: String){
        group = groupDao.getRoomById(idGroup).group

    }
    fun editGroup(){
        group!!.name = nameGroup
        group!!.avatar = avatarGroup
        for(id in listIDChoose){
            if(!group!!.members!!.contains(id)){
                group!!.members!!.add(id)
            }
        }
        for(id in listIDRemove){
            if(group!!.members!!.contains(id)){
                group!!.members!!.remove(id)
            }
        }
    }
    fun addGroup(){
        if (listIDChoose.size >= 3) {
            if (isEditGroup) {
                editGroup()
            } else {
                createGroup()
            }
        } else {
            Toast.makeText(getApplication(), "Please choose at least 3 friends", Toast.LENGTH_SHORT).show()
        }
    }
}