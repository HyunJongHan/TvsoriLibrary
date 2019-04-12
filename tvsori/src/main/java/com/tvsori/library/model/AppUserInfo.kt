package com.tvsori.library.model

import android.content.Context
import com.tvsori.library.readInt
import com.tvsori.library.readLong
import com.tvsori.library.readString

class AppUserInfo(msg: List<String>, type: Int) {
    var UserId: String
    var UserMode: Long = 0
    var NickName: String
    var Age: Int = 0
    var RoomId: Long = 0
    var Points: Long = 0

    init {
        when (type) {
            TYPE_USERINFO -> {
                UserId = msg.readString(1)
                UserMode = msg.readLong(2)
                NickName = msg.readString(3)
                Age = msg.readInt(5)
                RoomId = msg.readLong(10)
                Points = msg.readLong(13)
            }
            TYPE_ENTERROOM -> {
                RoomId = msg.readLong(1)
                UserId = msg.readString(2)
                UserMode = msg.readLong(3)
                NickName = msg.readString(4)
                Age = msg.readInt(6)
                Points = msg.readLong(15)
            }
            else -> {
                UserId = msg.readString(1)
                UserMode = msg.readLong(2)
                NickName = msg.readString(3)
                Age = msg.readInt(5)
                Points = msg.readLong(12)
                RoomId = msg.readLong(16)
            }
        }
    }

    fun isUserWoman(context: Context): Boolean {
        return (UserMode and 0xC0).ushr(6) == 0L
    }

    fun isDj(): Boolean {
        return (UserMode.ushr(32) and 0x20).ushr(5) == 1L
    }

    fun isOwner(): Boolean {
        return (UserMode.ushr(32) and 0x80).ushr(7) == 1L
    }

    fun isEmitor(): Boolean {
        return (UserMode.ushr(32) and 0x08).ushr(3) == 1L
    }

    fun isChat(): Boolean {
        return (UserMode and 0x20000).ushr(17) == 1L
    }

    fun isSemiOwner(): Boolean {
        return (UserMode.ushr(32) and 0x10).ushr(4) == 1L
    }

    fun isMobileUser(): Boolean {
        return (UserMode.ushr(32) and 0x8000).ushr(15) != 1L
    }

    fun getUserInfo(): String {
        return "$NickName ($UserId)"
    }

    companion object {
        const val TYPE_USERINFO = 1
        const val TYPE_MYINFO = 2
        const val TYPE_ENTERROOM = 3
    }
}