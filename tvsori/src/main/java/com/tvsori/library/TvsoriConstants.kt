package com.tvsori.library

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object TvsoriConstants {
    const val DEBUG_MODE = true

    const val DELIMITER = ""
    const val DELIMITER2 = ""
    const val DELIMITER3 = ""

    val USER_ITEMLIST: List<String> = Arrays.asList("001", "011", "081", "090", "601", "401", "411", "413", "444")

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("com.wesinglive.remocon", Context.MODE_PRIVATE)
    }

    // 20 + YY + MM + DD(01~12) + HH(24)
    var appVersionInfo : String by prefs.string("2018061021")

    var serverSocketIp : String by prefs.string("218.145.161.135")
    var serverSocketPort : Int by prefs.int(8085)
}