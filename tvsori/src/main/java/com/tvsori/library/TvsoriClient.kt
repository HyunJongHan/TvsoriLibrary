package com.tvsori.library

import android.content.Context
import com.tvsori.library.enums.ItemCode
import com.tvsori.library.model.AppBoardSongInfo
import com.tvsori.library.retrofit.TvsoriService

object TvsoriClient {
    private val tvsoriService = TvsoriService.create()
    private var userId: String? = null

    @Suppress("DEPRECATION")
    fun versionCheck(context: Context?, success: (isUpdate: Boolean) -> Unit, failure: (errMsg: String) -> Unit) {
        context?.apply {
            val version = packageManager.getPackageInfo(context.packageName, 0).versionCode
            tvsoriService.chkVersion(version.toString(), packageName).post { isSucc, rtnMsg ->
                if (context == null) {
                    return@post
                }
                when {
                    isSucc -> success(false)
                    rtnMsg.readString(0) == "update" -> success(true)
                    else -> failure(getString(R.string.tvsori_err_fail_network))
                }
            }
        }
    }

    fun loginUser(id: String, pass: String, success: (name: String) -> Unit, failure: (errMsg: String) -> Unit) {

    }

    fun joinUser(id: String, pass: String, name: String, sex: String = "male", birth: String = "810101", phone: String, success: (name: String) -> Unit, failure: (errMsg: String) -> Unit) {
        val deviceid = ""
        tvsoriService.joinAndroid(id, pass, name, sex, birth, deviceid).post { isSucc, rtnMsg ->
            if(isSucc) {
                success(rtnMsg.readString(0))
            } else {
                failure(rtnMsg.readString(0))
            }
        }
    }

    fun userItemList(userid: String, callback: (item: Map<ItemCode, Long>) -> Unit) {

    }

    fun userItem(userid: String, item: ItemCode, callback: (count: Long) -> Unit) {

    }

    fun memberExpiredDate(userid: String, item: ItemCode, callback: (date: Long, dateString: String) -> Unit) {

    }

    fun auditionList() {

    }

    fun favoriteSongList(userid: String, callback: (songlist: List<AppBoardSongInfo>) -> Unit) {

    }

    fun updateFavoriteSong(isAddSong: Boolean, userid: String, callback: ((isSucc: Boolean) -> Unit)? = null) {

    }

    fun userDuetProfile() {

    }

    fun updateDuetProfile(callback: ((isSucc: Boolean) -> Unit)? = null) {

    }
}