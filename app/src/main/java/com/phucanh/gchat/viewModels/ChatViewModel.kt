package com.phucanh.gchat.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    // TODO: Implement the ViewModel
    var mapAvatar = mutableMapOf<String?,String?>()
}