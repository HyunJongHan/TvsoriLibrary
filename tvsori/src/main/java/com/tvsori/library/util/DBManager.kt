package com.tvsori.library.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tvsori.library.model.AppBoardSongInfo
import com.tvsori.library.model.SongData
import java.util.*

object DBManager {
    /******************************************************************
     * Process Record Song
     */
    fun getRecordSongList(context: Context): ArrayList<SongData> {
        val helper = DBHelper(context)
        val db = helper.readableDatabase

        val query = "SELECT * FROM record_table ORDER BY regdate DESC"

        val data_list = ArrayList<SongData>()
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            data_list.add(SongData(cursor))
        }
        cursor.close()
        db.close()
        helper.close()
        return data_list
    }

    fun getRecentRecordSong(context: Context): SongData? {
        val helper = DBHelper(context)
        val db = helper.readableDatabase

        val query = "SELECT * FROM record_table ORDER BY regdate DESC"

        val data_list = ArrayList<SongData>()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            data_list.add(SongData(cursor))
        }
        cursor.close()
        db.close()
        helper.close()
        return if (data_list.size > 0) data_list[0] else null
    }

    fun insertRecordSong(context: Context, info: AppBoardSongInfo) {
        val helper = DBHelper(context)
        val db = helper.writableDatabase
        val add_values = ContentValues()
        add_values.put("number", info.SongNum)
        add_values.put("name", info.SongName)
        add_values.put("singer", info.Singer)
        add_values.put("filename", info.RequesterId)
        add_values.put("regdate", Date().time)
        db.insert("record_table", null, add_values)
        db.close()
        helper.close()
    }

    fun deleteRecordSong(context: Context, idx: Long) {
        val helper = DBHelper(context)
        val db = helper.writableDatabase
        db.delete("record_table", "_id = ?", arrayOf(idx.toString() + ""))
        db.close()
        helper.close()
    }

    fun getLong(cursor: Cursor, column: String): Long {
        try {
            return cursor.getLong(cursor.getColumnIndex(column))
        } catch (e: Exception) {
        }

        return 0
    }

    fun getString(cursor: Cursor, column: String): String {
        try {
            return cursor.getString(cursor.getColumnIndex(column))
        } catch (e: Exception) {
        }

        return ""
    }

    internal class DBHelper(context: Context) : SQLiteOpenHelper(context, "mym.db", null, 3) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("create table record_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "number TEXT, name TEXT, singer TEXT, filename TEXT, regdate INTEGER);")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("drop table if exists record_table")
            db.execSQL("create table record_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "number TEXT, name TEXT, singer TEXT, filename TEXT, regdate INTEGER);")
        }
    }
}
