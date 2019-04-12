package com.tvsori.library.retrofit.model

import com.google.gson.annotations.SerializedName

data class TvsoriArrayResponse(@SerializedName("rtnCd") val rtnCd: String = "fail", @SerializedName("rtnMsg") val rtnMsg: List<List<String>>?) {
    fun isSucc(): Boolean {
        return rtnCd == "success"
    }

    override fun toString(): String {
        return "TvsoriArrayResponse{rtnCd='$rtnCd', rtnMsg='$rtnMsg'}"
    }
}