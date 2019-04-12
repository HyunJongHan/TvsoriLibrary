package com.tvsori.library.model

import com.tvsori.library.readInt
import com.tvsori.library.readString

class AppChat(message: List<String>) {
    var UserId: String = message.readString(1)
    var NickName: String = message.readString(2)
    var ChatMode: Int = message.readInt(3)
    var Msg: String = message.readString(4)
}
