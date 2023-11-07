package com.phucanh.gchat.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(firebaseDatabase: FirebaseDatabase,application:Application) : AndroidViewModel(application) {

}