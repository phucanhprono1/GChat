package com.phucanh.gchat.viewModels

import android.app.Activity.RESULT_OK
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
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
    var doneCreate:Boolean ?= null
    var doneEdit:Boolean ?= null
    var isCreate=false
    var listIDChoose = HashSet<String>()
    var listIDRemove = HashSet<String>()
    var listFriend: ListFriend = ListFriend()
    var isEditGroup:Boolean? = null
    var group: Group? = null
    var avatarGroup: String? = null
    var nameGroup: String? = null
    var idGroup: String? = null
    var avatarGroupUri: Uri? = null
    val _listFriend: MutableLiveData<ListFriend?> = MutableLiveData()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        _listFriend.value = listFriend
        getListFriend()
    }

    fun getListFriend() {
        listFriend!!.listFriend = friendDao.getAll() as ArrayList<Friend?>
        _listFriend.value = listFriend
    }

    fun createGroup() {

        val idGroupCreate = (StaticConfig.UID + System.currentTimeMillis()).hashCode().toString()
        val groupCreate = Group()
        for (id in listIDChoose) {
            groupCreate.members.add(id)
        }
        groupCreate.id = idGroupCreate
        groupCreate.admin = StaticConfig.UID
        groupCreate.name = nameGroup
        groupCreate.avatar = avatarGroup

        firebaseDatabase.reference.child("group").child(idGroupCreate).setValue(groupCreate)
            .addOnCompleteListener {
                addRoomForUser(idGroupCreate, 0)

            }
            .addOnFailureListener() {
                Toast.makeText(getApplication(), "Create group failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteRoomForUser(roomId: String, userIndex: Int) {
        if (userIndex == listIDRemove.size) {
//            doneEdit = true
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
            if (isEditGroup == false) {
//                doneCreate = true
                return
            } else {
//                doneEdit = false
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

    fun getGroup(idGroup: String): Group {
        var listMem = groupDao.getRoomById(idGroup) as ArrayList<GroupMember>

        var groupGet = group ?: Group() // Use the existing group or create a new one
        groupGet.id = idGroup
        for (mem in listMem) {
            groupGet.members.add(mem.id)
            groupGet.admin = mem.group!!.admin
            groupGet.name = mem.group!!.name
            groupGet.avatar = mem.group!!.avatar
        }
        return groupGet
    }

    fun editGroup() {
        var groupEdit = Group()
        groupEdit.id = idGroup!!
        groupEdit.admin = mAuth.currentUser!!.uid
        groupEdit.name = nameGroup
        groupEdit.avatar = avatarGroup
        for (id in listIDChoose) {
            groupEdit.members.add(id)
        }
        firebaseDatabase.reference.child("group").child(idGroup!!).setValue(groupEdit)
            .addOnCompleteListener {
                addRoomForUser(idGroup!!, 0)
            }.addOnFailureListener() {
            Toast.makeText(getApplication(), "Edit group failed", Toast.LENGTH_SHORT).show()
        }

    }
}