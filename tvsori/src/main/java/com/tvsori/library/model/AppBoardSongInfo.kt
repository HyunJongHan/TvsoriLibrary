package com.tvsori.library.model

import com.tvsori.library.readLong
import com.tvsori.library.readString

class AppBoardSongInfo(msg: List<String>, isZiller: Boolean = false) {
    val SongId: Long
    val SongNum: String
    val SongName: String
    val Singer: String
    var RequesterId: String
    var RequesterNickName: String

    init {
        if (isZiller) {
            SongId = 0
            SongNum = msg.readString(0)
            SongName = msg.readString(1)
            Singer = msg.readString(2)
            RequesterId = ""
            RequesterNickName = ""
        } else {
            SongId = msg.readLong(2)
            SongNum = msg.readString(3)
            SongName = msg.readString(4)
            Singer = msg.readString(5)
            RequesterId = msg.readString(6)
            RequesterNickName = msg.readString(7)
        }
    }
}
