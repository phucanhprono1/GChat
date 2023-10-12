package com.phucanh.gchat.models

class Friend(id: String, name: String, email: String, avt: String, var idRoom: String) :
    User(id, name, email, avt)