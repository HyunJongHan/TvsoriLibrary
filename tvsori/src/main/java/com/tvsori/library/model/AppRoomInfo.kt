package com.tvsori.library.model

import com.tvsori.library.TvsoriConstants
import com.tvsori.library.readInt
import com.tvsori.library.readLong
import com.tvsori.library.readString

class AppRoomInfo {
    var RoomId: Long = 0
    var RoomMode: Long = 0
    var RoomName: String
    var RoomTheme: String
    var OwnerId: String
    var OwnerName: String
    var MaxUsers: Int = 0
    var CurrUsers: Int = 0
    var LoAge: Int = 0
    var HiAge: Int = 0

    private var category: String

    constructor(msg: List<String>, isCreate: Boolean) {
        val _roomname = msg.readString(3).split(TvsoriConstants.DELIMITER)
        val _roomtheme = msg.readString(4).split(TvsoriConstants.DELIMITER)

        RoomId = msg.readLong(1)
        RoomMode = msg.readLong(2)
        RoomName = _roomname.readString(0)
        RoomTheme = _roomtheme.readString(0)
        OwnerId = msg.readString(8)
        OwnerName = msg.readString(9)
        MaxUsers = msg.readInt(12)
        CurrUsers = if (isCreate) 1 else msg.readInt(13)
        LoAge = msg.readInt(if (isCreate) 13 else 14)
        HiAge = msg.readInt(if (isCreate) 14 else 15)

        category = _roomtheme.readString(1)
    }

    constructor(msg: List<String>) {
        val _roomname = msg.readString(6).split(TvsoriConstants.DELIMITER)
        val _roomtheme = msg.readString(7).split(TvsoriConstants.DELIMITER)

        RoomId = msg.readLong(0)
        RoomMode = msg.readLong(1)
        RoomName = _roomname.readString(0)
        RoomTheme = _roomtheme.readString(0)
        OwnerId = msg.readString(8)
        OwnerName = msg.readString(9)
        MaxUsers = msg.readInt(4)
        CurrUsers = msg.readInt(5)
        LoAge = msg.readInt(2)
        HiAge = msg.readInt(3)

        category = _roomtheme.readString(1)
    }

    val isGhostRoom: Boolean get() = RoomMode and 0x00000100 > 0

    val isFixedRoom: Boolean get() = RoomMode and 0x01 > 0

    val isPasswordRoom: Boolean get() = RoomMode and 0x04 > 0

    val isPublicRoom: Boolean get() = RoomMode and 0x00002000 > 0

    val roomUserAge: String
        get() {
            return if (LoAge == 0 && HiAge == 99) {
                "Anyone"
            } else if (HiAge == 99) {
                LoAge.toString() + "Over"
            } else {
                LoAge.toString() + "~" + HiAge + "Age"
            }
        }

    fun isCategory(category_id: String?): Boolean {
        return category == category_id
    }

    fun changeRoomInfo(msg: List<String>) {
        val param = msg.readLong(2)
        val _roomname = msg.readString(4).split(TvsoriConstants.DELIMITER)
        if (param and 0x00000010 > 0) {
            RoomName = _roomname.readString(0)
        }
        if (param and 0x00000040 > 0) {
            OwnerId = msg.readString(9)
        }
    }
}