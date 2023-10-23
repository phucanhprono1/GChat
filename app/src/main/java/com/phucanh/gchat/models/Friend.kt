package com.phucanh.gchat.models

import android.os.Parcel
import android.os.Parcelable

class Friend(id: String, name: String, email: String, avt: String, var idRoom: String?) :
    User(id, name, email, avt) {
    constructor(parcel: Parcel) : this(
        TODO("id"),
        TODO("name"),
        TODO("email"),
        TODO("avt"),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(idRoom)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel): Friend {
            return Friend(parcel)
        }

        override fun newArray(size: Int): Array<Friend?> {
            return arrayOfNulls(size)
        }
    }
}