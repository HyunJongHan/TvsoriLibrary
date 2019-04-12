package com.tvsori.library.model

import com.tvsori.library.readInt
import com.tvsori.library.readString

class AppBoardMsgInfo(msg: List<String>) {
    val MsgId = msg.readInt(2)
    val NickName = msg.readString(4)
    val Title = msg.readString(5)
    val Body = msg.readString(6)
}