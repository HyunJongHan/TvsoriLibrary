package com.tvsori.library.model

import android.database.Cursor
import android.os.Environment
import com.tvsori.library.util.DBManager
import java.text.SimpleDateFormat
import java.util.*

class SongData(c: Cursor) {
    var id: Long = 0
    var title: String
    var artist: String
    var filename: String
    var filepath: String
    private var regdate: Long = 0
    var str_date: String

    init {
        id = DBManager.getLong(c, "_id")
        title = DBManager.getString(c, "name")
        artist = DBManager.getString(c, "singer")
        filename = DBManager.getString(c, "filename")
        filepath = Environment.getExternalStorageDirectory().absolutePath + "/mym/" + filename
        regdate = DBManager.getLong(c, "regdate")
        str_date = SimpleDateFormat("yyyy.MM.dd").run { format(Date(regdate)) }

    }
}
