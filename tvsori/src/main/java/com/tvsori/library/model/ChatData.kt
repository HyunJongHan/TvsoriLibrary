package com.tvsori.library.model

import com.tvsori.library.TvsoriConstants
import com.tvsori.library.readString

class ChatData {
    var type: Int = 0
    var name: String
    var msg: String
    var image_list: Array<String>? = null

    constructor(type: Int, data: AppChat) {
        this.type = type
        this.name = data.NickName
        this.msg = data.Msg.split(TvsoriConstants.DELIMITER).readString(7).replace("\\+", " ")
        this.msg = msg.replace("\n".toRegex(), "")
        this.msg = msg.replace("\r".toRegex(), "")
    }

    constructor(type: Int, nickname: String, msg: String) {
        this.type = type
        this.name = nickname
        this.msg = msg
    }

    constructor(type: Int, nickname: String, image_list: Array<String>) {
        this.type = type
        this.name = nickname
        this.msg = ""
        this.image_list = image_list
    }

    constructor(type: Int, info: String) {
        this.type = type
        this.name = ""
        this.msg = info
    }

    companion object {
        const val TYPE_INFO = 1
        const val TYPE_ME = 2
        const val TYPE_USER = 3
        const val TYPE_USER_INFO = 4
        const val TYPE_ME_INFO = 5
        const val TYPE_NOTI = 6
        const val TYPE_IMAGE = 7
        const val TYPE_GIF = 8
    }
}