package com.tvsori.library.model

import com.tvsori.library.readInt
import com.tvsori.library.readLong
import com.tvsori.library.readString

class SongContestData(array: List<String>) {
    var contest_id: String = array.readString(0)
    var userid: String = array.readString(1)
    var usernick: String = array.readString(2)
    var filepath: String = "http://m.tvsori.com/vod/" + array.readString(3)
    var subject: String = array.readString(4)
    private var regdate: String = array.readString(5)
    var point_count: Int = array.readInt(6)
    var comment_cnt: Long = array.readLong(8)
    var view_count: Int = array.readInt(7)

//    var date: Date = DateUtil.parseDate(regdate)
//    var imgUrl: String = if (filepath.length > 3) filepath.substring(0, filepath.length - 3) + "jpg" else ""
//
//    var list_dj: Spanned = Utils.fromHtml(String.format("<font color='#1eb9ee'>%s</font> %s", usernick, userid))
//    var list_info: String = getDate()

    private fun getDate(): String {
        //return SimpleDateFormat("yyyy.MM.dd").format(date)
        return ""
    }

    // AUDITION / SONG LIST / RECORD LIST 는 내부적으로 처리!!!!!!!
}
