package com.tvsori.library

import android.app.Application

open class TvsoriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TvsoriConstants.init(this@TvsoriApplication)
    }
}