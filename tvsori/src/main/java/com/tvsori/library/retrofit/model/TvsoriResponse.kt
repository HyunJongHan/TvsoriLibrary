package com.tvsori.library.retrofit.model

import com.google.gson.annotations.SerializedName

data class TvsoriResponse(@SerializedName("rtnCd") val rtnCd: String = "fail", @SerializedName("rtnMsg") val rtnMsg: List<String>?) {
    fun isSucc(): Boolean {
        return rtnCd == "success"
    }

    override fun toString(): String {
        return "TvsoriResponse{rtnCd='$rtnCd', rtnMsg='$rtnMsg'}"
    }
}
