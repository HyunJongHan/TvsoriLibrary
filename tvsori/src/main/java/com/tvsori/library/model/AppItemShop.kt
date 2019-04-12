package com.tvsori.library.model

import com.tvsori.library.enums.ItemShopMode
import com.tvsori.library.readInt
import com.tvsori.library.readLong
import com.tvsori.library.readString
import java.util.*

class AppItemShop(msg: List<String>) {
    var SubCmd: ItemShopMode =
        if (msg.readInt(1) < ItemShopMode.values().size) ItemShopMode.values()[msg.readInt(1)] else ItemShopMode.UnshareItem
    var UserId: String = msg.readString(2)
    var Target: String = msg.readString(3)
    var ItemId: String = msg.readString(4)
    var Count: Long = msg.readLong(5)
    var Count1: Long = msg.readLong(6)
    var Count3: Long = msg.readLong(8)
    private var Items = HashMap<String, List<String>>()

    init {
        if (SubCmd === ItemShopMode.GetRoomItems) {
            val itemlist = msg.readString(10).split(",")
            for (item in itemlist) {
                val i_list = item.split(":".toRegex())
                val code = i_list.readString(0)
                if (ROOM_ITEMLIST.contains(code)) {
                    Items[code] = i_list
                }
            }
        }
    }

    fun getItem(code: String): List<String>? {
        return Items[code]
    }

    fun getItem(code: String, position: Int): Long {
        val item = Items[code]
        return item?.readLong(position) ?: 0
    }

    companion object {
        private val USER_ITEMLIST = Arrays.asList("001", "011", "081", "090", "601", "401", "411", "413", "444")
        private val ROOM_ITEMLIST = Arrays.asList("001")
    }
}
