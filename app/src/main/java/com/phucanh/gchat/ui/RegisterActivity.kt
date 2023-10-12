package com.phucanh.gchat.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.phucanh.gchat.R
import com.phucanh.gchat.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}