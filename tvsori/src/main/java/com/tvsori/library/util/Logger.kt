package com.tvsori.library.util

import android.util.Log
import com.tvsori.library.BuildConfig

object Logger {
    fun log(msg: String, e: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e("TVSORI", msg, e)
        }
    }
}