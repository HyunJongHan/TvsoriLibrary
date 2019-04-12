package com.tvsori.library

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import android.util.Log
import com.tvsori.library.enums.InCmd
import com.tvsori.library.enums.ItemShopMode
import com.tvsori.library.enums.OutCmd
import com.tvsori.library.enums.ServerErrorCode
import com.tvsori.library.model.*
import com.tvsori.library.util.Logger
import com.tvsori.library.util.TvsoriItemUtil
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class TvsoriLoader(private val context: Context, private var loaderCallback: LoaderCallback?) {
    interface LoaderCallback {
        fun onDisconnected(isNetworkError: Boolean)
        fun onOverlapLogin()
        fun onEnterBroadcastRoom(isSucc: Boolean, roomId: Long, errMessage: String?)
        fun onKickOutBroadcastRoom(keyOutMessage: String)
        fun onChangeBroadcastUser(djId: String?, emitorId: String?)
        fun onRefreshUserItem()
    }

    private val CHAT_OBJECT = Object()

    private var roomChatList: MutableList<ChatData> = mutableListOf()
    private var roomUserHashMap: MutableMap<String, AppUserInfo> = mutableMapOf()
    private var userItemHashMap: MutableMap<String, List<String>> = mutableMapOf()

    private var tvsoriServer: TvsoriServer? = null
    private var msgQueue: MessageThread = MessageThread()

    private var pingTimer: Timer? = null
    private var removeNotiTimer: Timer? = null

    private var isConnectionLock: Boolean = false

    private lateinit var userId: String
    private lateinit var userPass: String
    private lateinit var loginType: String
    private var userMode: Long = 0L
    private var roomId: Long = 0L
    private var isCreateRoom: Boolean = false
    private var enterRoomMessage: Array<String?> = arrayOf()

    private var userInfo: AppUserInfo? = null
    private var roomInfo: AppRoomInfo? = null
    private var roomItemShop: AppItemShop? = null

    private var roomDjId: String? = null
    private var roomEmitorId: String? = null

    fun setCreateBroadcastRoom(userId: String, userNickname: String, title: String, password: String, category: String, person: Int, minAge: Int, maxAge: Int, isPublic: Boolean) {
        isCreateRoom = true
        val roommode: Long = if (isPublic && !TextUtils.isEmpty(password)) {
            0xF143004L
        } else if (!TextUtils.isEmpty(password)) {
            0xF141004L
        } else if (isPublic) {
            0xF143010L
        } else {
            0xF141010L
        }

        enterRoomMessage = arrayOfNulls(25)
        enterRoomMessage[0] = (Random().nextFloat() * 10000000).toLong().toString()
        enterRoomMessage[1] = roommode.toString()
        enterRoomMessage[2] = title + TvsoriConstants.DELIMITER + TvsoriConstants.DELIMITER
        enterRoomMessage[3] = "Song" + TvsoriConstants.DELIMITER + category + TvsoriConstants.DELIMITER + "No_Limit"
        enterRoomMessage[4] = "DanceRoom"
        enterRoomMessage[5] = if (TextUtils.isEmpty(password)) "NULL" else password
        enterRoomMessage[6] = if (TextUtils.isEmpty(password)) "NULL" else password
        enterRoomMessage[7] = userId
        enterRoomMessage[8] = userNickname
        enterRoomMessage[9] = userId
        enterRoomMessage[10] = userNickname
        enterRoomMessage[11] = person.toString()
        enterRoomMessage[12] = minAge.toString()
        enterRoomMessage[13] = maxAge.toString()
        enterRoomMessage[14] = "1"
        enterRoomMessage[15] = "3"
        enterRoomMessage[16] = "0"
        enterRoomMessage[17] = "0"
        enterRoomMessage[18] = "0"
        enterRoomMessage[19] = "0"
        enterRoomMessage[20] = "0"
        enterRoomMessage[21] = "0"
        enterRoomMessage[22] = "NULL"
        enterRoomMessage[23] = "NULL"
        enterRoomMessage[24] = "0"
    }

    fun setEnterBroadcastRoom(roomid: Long, password: String?) {
        isCreateRoom = false
        roomId = roomid
        enterRoomMessage = arrayOf(roomid.toString(), password ?: "NULL", "1")
    }

    fun doLoginTvsoriServer(userid: String, userpass: String = "guest", usermode: Long = 0x14000003A4AAL) {
        loginType = "-2"
        userId = userid
        userPass = userpass
        userMode = usermode

        msgQueue.start()

        pingTimer = Timer().apply { schedule(PingTimerTask(), 60000, 30000) }

        if (tvsoriServer != null) {
            if (tvsoriServer?.isConnected() == true) {
                if (userInfo?.RoomId == roomId) {
                    return
                }
            }
        }
        if (isConnectedNetwork(context) && !isConnectionLock) {
            if (tvsoriServer != null) {
                tvsoriServer?.interrupt()
                tvsoriServer = null
            }
            isConnectionLock = true
            tvsoriServer = TvsoriServer(coonectCallback = ::onConnectionCallback, msgCallback = ::onServerMessageCallback).apply { start() }
        }
    }

    fun doLogoutTvsoriServer() {
        try {
            stopTvsoriServer(true)
        } catch (e: Exception) {
        }
    }

    private fun stopTvsoriServer(isLogout: Boolean) {
        loaderCallback = null

        pingTimer?.cancel()
        pingTimer = null

        removeNotiTimer?.cancel()
        removeNotiTimer = null

        msgQueue.interrupt()

        if (userId.isNotEmpty() && isLogout) {
            tvsoriServer?.doLogout(userId)
        } else {
            try {
                tvsoriServer?.interrupt()
            } catch (e: Exception) {
            }
        }
        tvsoriServer = null
    }

    fun presentItemToUser(itemcode: String, target: String, count: Long) {
        msgQueue.put(OutCmd.ITEMSHOP, arrayOf("4", userId, target, itemcode, count.toString(), "0", "0", "0", roomId.toString(), "NULL"))
    }

    fun giveItemToUser(itemcode: String, target: String, count: Long) {
        msgQueue.put(OutCmd.ITEMSHOP, arrayOf("5", userId, target, itemcode, count.toString(), "0", "0", "0", roomId.toString(), "NULL"))
    }

    fun refreshUserItem() {
        msgQueue.put(OutCmd.ITEMSHOP, arrayOf("0", userId, "NULL", "NULL", "0", "0", "0", "0", "0", "NULL"))
    }

    fun getChatData(position: Int): ChatData {
        synchronized(CHAT_OBJECT) {
            try {
                return roomChatList[position]
            } catch (e: Exception) {
            }
            return ChatData(ChatData.TYPE_INFO, "")
        }
    }

    fun getChatListSize(): Int {
        synchronized(CHAT_OBJECT) {
            return roomChatList.size
        }
    }

    private fun appendSystemMessage(message: String) {
        synchronized(CHAT_OBJECT) {
            roomChatList.add(ChatData(ChatData.TYPE_INFO, message))
        }
    }

    private fun appendTvsoriMessage(chat: AppChat, userid: String) {
        var type = if (chat.UserId == userid) ChatData.TYPE_ME else ChatData.TYPE_USER
        if (chat.ChatMode == 1) {
            type = if (type == ChatData.TYPE_ME) ChatData.TYPE_ME_INFO else ChatData.TYPE_USER_INFO
        }
        val data = ChatData(type, chat)
        if (data.msg.contains("") || data.msg.contains("<img src=")) {
            data.msg = data.msg.replace(" ", "")
            val image_list = mutableListOf<String>()
            val gif_list = mutableListOf<String>()
            var sb = StringBuilder()
            var split = data.msg.split("")
            if (split[0].isNotEmpty()) {
                sb.append(split[0])
            }
            for (i in 1 until split.size) {
                val s = split[i]
                val id = context.resources.getIdentifier(s.substring(0, 3), "drawable", context.packageName)
                if (id > 0) {
                    gif_list.add("imo_" + s.substring(0, 3))
                }
                if (s.length > 3) {
                    sb.append(s.substring(3))
                }
            }
            val new_msg = sb.toString()
            if (new_msg.contains("<imgsrc=")) {
                sb = StringBuilder()
                split = new_msg.split("<imgsrc=".toRegex())
                if (split[0].isNotEmpty()) {
                    sb.append(split[0])
                }
                for (i in 1 until split.size) {
                    val s = split[i]
                    val index = s.indexOf(">")
                    if (index > 2) {
                        image_list.add(s.substring(1, index - 1))
                        if (s.length > index + 1) {
                            sb.append(s.substring(index + 1))
                        }
                    }
                }
            }
            synchronized(CHAT_OBJECT) {
                if (sb.isNotEmpty()) {
                    roomChatList.add(ChatData(type, data.name, sb.toString()))
                }
                if (gif_list.size > 0) {
                    roomChatList.add(ChatData(ChatData.TYPE_GIF, data.name, gif_list.toTypedArray()))
                }
                if (image_list.size > 0) {
                    roomChatList.add(ChatData(ChatData.TYPE_IMAGE, data.name, image_list.toTypedArray()))
                }
                if (roomChatList.size > 1000) {
                    while (roomChatList.size > 500) {
                        roomChatList.removeAt(0)
                    }
                }
            }
        } else {
            synchronized(CHAT_OBJECT) {
                roomChatList.add(ChatData(type, chat))
                if (roomChatList.size > 1000) {
                    while (roomChatList.size > 500) {
                        roomChatList.removeAt(0)
                    }
                }
            }
        }
    }


    private fun getUserInfo(userid: String): AppUserInfo? {
        return roomUserHashMap[userid]
    }

    private fun getUserNickname(userid: String): String {
        return roomUserHashMap[userid]?.NickName ?: userid
    }

    private fun getUserInfoString(userid: String): String {
        return roomUserHashMap[userid]?.getUserInfo() ?: userid
    }

    private fun getBroadcastMainId(djid: String, myid: String, room_layout: List<String>): String {
        for (i in room_layout.indices) {
            val layout = room_layout[i].split(TvsoriConstants.DELIMITER3.toRegex())
            val userid = layout.readString(0)
            if (!TextUtils.isEmpty(userid) && userid == myid && layout.readInt(1) == 0) {
                return userid
            }
        }
        for (i in room_layout.indices) {
            val layout = room_layout[i].split(TvsoriConstants.DELIMITER3.toRegex())
            val userid = layout.readString(0)
            if (!TextUtils.isEmpty(userid) && userid == djid && layout.readInt(1) == 0) {
                return userid
            }
        }
        for (i in room_layout.indices) {
            val layout = room_layout[i].split(TvsoriConstants.DELIMITER3.toRegex())
            val userid = layout.readString(0)
            if (!TextUtils.isEmpty(userid) && layout.readInt(1) == 0) {
                return userid
            }
        }
        return ""
    }

    private fun getBroadcastSubId(room_layout: List<String>): String {
        for (i in room_layout.indices) {
            val layout = room_layout[i].split(TvsoriConstants.DELIMITER3.toRegex())
            val userid = layout.readString(0)
            if (!TextUtils.isEmpty(userid) && layout.readInt(1) == 1) {
                return userid
            }
        }
        return ""
    }

    private fun onConnectionCallback(isConnected: Boolean, isNetworkError: Boolean) {
        if (isConnected) {
            tvsoriServer?.sendMessage(OutCmd.LOGIN, arrayOf(userId, userPass, userMode.toString(), TvsoriConstants.appVersionInfo, "0", "0", loginType, "AT", "MOBblTSQvrj", "0"))
        } else {
            loaderCallback?.onDisconnected(isNetworkError)
        }
    }

    private fun onServerMessageCallback(cmd: InCmd, errCode: ServerErrorCode, msg: List<String>) {
        if (cmd === InCmd.MYINFO) {
            if (errCode === ServerErrorCode.RERR_NONE) {
                roomChatList.clear()
                isConnectionLock = false
                userInfo = AppUserInfo(msg, AppUserInfo.TYPE_MYINFO)

                roomChatList.clear()
                roomUserHashMap.clear()

            } else if (errCode === ServerErrorCode.RERR_LOGIN_LOGOUT_RELOGIN) {
                loaderCallback?.onOverlapLogin()
            }
        } else if (cmd === InCmd.CHATMSG) {
            appendTvsoriMessage(AppChat(msg), userId)
        } else if (cmd == InCmd.ROOMLIST) {
            userInfo?.let { user ->
                if (user.RoomId == 0L) {
                    if (isCreateRoom) {
                        msgQueue.put(OutCmd.CREATEROOM, enterRoomMessage)
                    } else {
                        msgQueue.put(OutCmd.ENTERROOM, enterRoomMessage)
                    }
                } else if (roomId != user.RoomId) {
                    loaderCallback?.onDisconnected(false)
                } else {
                    loginType = "-3"
                    loaderCallback?.onEnterBroadcastRoom(true, roomId, null)
                }
                return@let
            }
        } else if (cmd === InCmd.USERINFO) {
            val user = AppUserInfo(msg, AppUserInfo.TYPE_USERINFO)
            if (roomId > 0 && user.RoomId == roomId) {
                roomUserHashMap[user.UserId] = user
            }
        } else if (cmd === InCmd.CREATEROOM) {
            val room = AppRoomInfo(msg, true)
            if (errCode === ServerErrorCode.RERR_OWNER_OF_OTHER_ROOM) {
                loaderCallback?.onEnterBroadcastRoom(false, room.RoomId, context.getString(R.string.tvsori_room_create_fail_1))
                return
            }
            if (userId == msg.readString(8)) {
                if (errCode === ServerErrorCode.RERR_NONE) {
                    roomId = room.RoomId
                    loginType = "-3"
                    roomInfo = room

                    removeNotiTimer = Timer().apply { schedule(RemoteNotiTimerTask(), 5000) }

                    val layout = mutableListOf<String>()
                    layout.add("0")
                    layout.add(room.RoomId.toString())
                    layout.add(TextUtils.join(TvsoriConstants.DELIMITER3, arrayOf("1", "1")))

                    val l1 = TextUtils.join(TvsoriConstants.DELIMITER3, arrayOf(room.OwnerId, "0", "0", "0", "0", "0", "640", "480"))
                    val l2 = TextUtils.join(TvsoriConstants.DELIMITER3, arrayOf("", "1", "0", "0", "0", "0", "640", "480"))
                    val l3 = TextUtils.join(TvsoriConstants.DELIMITER3, arrayOf("", "-1", "0", "0", "0", "0", "0", "0"))
                    layout.add(TextUtils.join(TvsoriConstants.DELIMITER2, arrayOf(l1, l2, l3)))
                    msgQueue.put(OutCmd.LAYOUT, layout.toTypedArray())
                    loaderCallback?.onEnterBroadcastRoom(true, roomId, null)
                } else {
                    loaderCallback?.onEnterBroadcastRoom(false, 0L, context.getString(R.string.tvsori_room_create_fail_2))
                }
            }
        } else if (cmd === InCmd.ENTERROOM) {
            val user = AppUserInfo(msg, AppUserInfo.TYPE_ENTERROOM)
            roomUserHashMap[user.UserId] = user
            if (user.UserId == userId) {
                if (errCode == ServerErrorCode.RERR_NONE) {
                    roomId = user.RoomId
                    removeNotiTimer = Timer().apply { schedule(RemoteNotiTimerTask(), 8000) }
                    loaderCallback?.onEnterBroadcastRoom(true, roomId, null)
                } else {
                    loaderCallback?.onEnterBroadcastRoom(false, roomId, context.getString(R.string.tvsori_room_enter_fail_1))
                }
            } else {
                appendSystemMessage(context.getString(R.string.tvsori_user_enterroom, user.getUserInfo()))
            }
        } else if (cmd === InCmd.EXITROOM) {
            val userid = msg.readString(2)
            roomUserHashMap.remove(userid)
        } else if (cmd === InCmd.KICKOUT) {
            roomUserHashMap.remove(msg.readString(1))

            val userid = msg.readString(1)
            val fromid = msg.readString(3)
            val usernick = getUserNickname(userid)
            val fromnick = getUserNickname(fromid)
            if (userId == userid) {
                loaderCallback?.onKickOutBroadcastRoom(context.getString(R.string.tvsori_user_kickout))
            } else {
                appendSystemMessage(context.getString(R.string.tvsori_user_kickout, usernick, fromnick))
            }
        } else if (cmd === InCmd.CHANGEUSERMODE) {
            val userid = msg.readString(1)
            val user = getUserInfo(userid) ?: return
            val from = msg.readString(5)
            val from_user = getUserInfoString(from)
            val old_mode = user.UserMode
            val cur_mode = msg.readLong(4)
            user.UserMode = cur_mode

            when (cur_mode xor old_mode) {
                0x0000001000000000L -> //ModeHelper
                    appendSystemMessage(context.getString(if (user.isSemiOwner()) R.string.tvsori_semiowner_succ else R.string.tvsori_semiowner_fail, user.NickName))
                0x0000008000000000L -> //ModeOwner
                    appendSystemMessage(if (user.isOwner()) context.getString(R.string.tvsori_owner_succ, user.NickName) else "")
                0x0000000000020000L -> //ModeAllowSendRoomChat
                    appendSystemMessage(context.getString(if (!user.isChat()) R.string.tvsori_mute_succ else R.string.tvsori_mute_fail, user.NickName))
                0x0000000800000000L -> //ModeEmitor
                    appendSystemMessage(context.getString(if (user.isEmitor()) R.string.tvsori_minicam_succ else R.string.tvsori_minicam_fail_1, user.NickName))
                0x0000002000000000L -> //ModeDJ
                    appendSystemMessage(if (user.isDj()) context.getString(R.string.tvsori_dj_set, from_user, user.getUserInfo()) else "")
            }
        } else if (cmd === InCmd.CHANGEROOMINFO) {
            val roomid = msg.readLong(1)
            if (roomid == roomId) {
                roomInfo?.changeRoomInfo(msg)
            }
        } else if (cmd === InCmd.ADDBOARDSONG) {
            val nick = msg.readString(7)
            val title = msg.readString(4)
            appendSystemMessage(context.getString(R.string.tvsori_user_reserv_song, nick, title))
        } else if (cmd === InCmd.ITEMSHOP) {
            val item = AppItemShop(msg)
            if (item.SubCmd === ItemShopMode.GetUserItems) {
                userItemHashMap.clear()
                val itemlist = msg.readString(10).split(",")
                for (_item in itemlist) {
                    val i_list = _item.split(":".toRegex())
                    val code = i_list.readString(0)
                    if (TvsoriConstants.USER_ITEMLIST.contains(code)) {
                        userItemHashMap[code] = i_list
                    }
                }
                loaderCallback?.onRefreshUserItem()
            } else if (item.SubCmd === ItemShopMode.GetRoomItems) {
                roomItemShop = item
            } else if (item.SubCmd === ItemShopMode.GiveItemToUser) {
                var from = getUserInfoString(item.UserId)
                val to = getUserInfoString(item.Target)
                if (TextUtils.isEmpty(from) || from == "NULL") {
                    from = context.getString(R.string.tvsori_user_anonymus)
                }
                if (item.ItemId == "001") { // 곡 선물
                    if (msg.readInt(5) >= 200) {
                        if (item.UserId == userId || item.Target == userId) {
                            appendSystemMessage(context.getString(R.string.tvsori_item_get_song, from, to, msg.readString(5)))
                        }
                    } else {
                        appendSystemMessage(context.getString(R.string.tvsori_item_get_song, from, to, msg.readString(5)))
                    }
                }
            } else if (item.SubCmd === ItemShopMode.PresentItemToUser) {
                var from = getUserInfoString(item.UserId)
                val to = getUserInfoString(item.Target)
                if (TextUtils.isEmpty(from) || from == "NULL") {
                    from = context.getString(R.string.tvsori_user_anonymus)
                }
                if (item.ItemId == "601") { // 하트콘
                    appendSystemMessage(context.getString(R.string.tvsori_item_send_heartcon, from, to, msg.readString(5)))
                    appendSystemMessage(context.getString(R.string.tvsori_item_get_heartcon, to, msg.readString(8)))
                } else {
                    val name = TvsoriItemUtil.getGiftItemName(item.ItemId)
                    val point = TvsoriItemUtil.getGiftItemPoint(item.ItemId)
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(point)) {
                        appendSystemMessage(context.getString(R.string.tvsori_item_send_point, from, to, name, point))
                    }
                    appendSystemMessage(context.getString(R.string.tvsori_item_get_point, to, msg.readString(6)))
                }
            }
        } else if (cmd === InCmd.LAYOUT) {
            val layout = msg.readString(4).split(TvsoriConstants.DELIMITER2.toRegex())
            roomDjId = getBroadcastMainId(roomDjId ?: "", userId, layout)
            roomEmitorId = getBroadcastSubId(layout)
            loaderCallback?.onChangeBroadcastUser(roomDjId, roomEmitorId)
        }
    }

    private fun isConnectedNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.activeNetworkInfo != null) {
            return cm.activeNetworkInfo.isConnected
        }
        return false
    }

    inner class MessageData(var cmd: OutCmd, var msg: Array<String?>)

    inner class MessageThread : Thread() {
        private val queue = ArrayBlockingQueue<MessageData>(10)
        override fun run() {
            super.run()
            do {
                try {
                    val data = queue.take()
                    Logger.log("SEND MESSAGE CMD = ${data.cmd} / MSG = ${data.msg.toList()}")
                    tvsoriServer?.sendMessage(data.cmd, data.msg)
                } catch (e: Exception) {
                    Log.e("TEST", "SEND EXCEPTION", e)
                }
            } while (!isInterrupted)
        }

        fun put(cmd: OutCmd, msg: Array<String?>) {
            queue.put(MessageData(cmd, msg))
        }
    }

    inner class PingTimerTask : TimerTask() {
        override fun run() {
            if (isConnectedNetwork(context)) {
                msgQueue.put(OutCmd.PING, arrayOfNulls(0))
            }
        }
    }

    inner class RemoteNotiTimerTask : TimerTask() {
        init {
            synchronized(CHAT_OBJECT) {
                roomChatList.add(ChatData(ChatData.TYPE_NOTI, context.getString(R.string.tvsori_chat_noti_1)))
                roomChatList.add(ChatData(ChatData.TYPE_NOTI, context.getString(R.string.tvsori_chat_noti_2)))
                roomChatList.add(ChatData(ChatData.TYPE_NOTI, context.getString(R.string.tvsori_chat_noti_3)))
                roomChatList.add(ChatData(ChatData.TYPE_NOTI, context.getString(R.string.tvsori_chat_noti_4)))
                return@synchronized
            }
        }

        override fun run() {
            synchronized(CHAT_OBJECT) {
                val i = roomChatList.iterator()
                while (i.hasNext()) {
                    val data = i.next()
                    if (data.type == ChatData.TYPE_NOTI) {
                        i.remove()
                    }
                }
            }
            removeNotiTimer = null
        }
    }
}