@file:JvmName("-KotlinExtensions")

package com.tvsori.library

import android.content.SharedPreferences
import android.os.Build
import android.text.Html
import android.text.Spanned
import com.tvsori.library.model.AppBoardSongInfo
import com.tvsori.library.retrofit.model.TvsoriArrayResponse
import com.tvsori.library.retrofit.model.TvsoriResponse
import com.tvsori.library.util.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Int.rangeString(digits: Int): String {
    val s = StringBuffer(digits)
    try {
        val zeroes = digits - (Math.log(this.toDouble()) / Math.log(10.0)).toInt() - 1
        for (i in 0 until zeroes) {
            s.append(0)
        }
    } catch (e: Exception) {
    }
    return s.append(this).toString()
}

fun Long.longToString(): String {
    try {
        return DecimalFormat("#,###").format(this.toDouble())
    } catch (e: Exception) {
    }
    return this.toString()
}

fun String.parseInt(): Int {
    try {
        return this.toInt()
    } catch (e: Exception) {
    }
    return 0
}

@Suppress("DEPRECATION")
fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun String.parseLong(): Long {
    try {
        return this.toLong()
    } catch (e: Exception) {
    }
    return 0
}

fun List<String>.readString(index: Int): String {
    try {
        val v = this[index]
        return if (v.isEmpty() || v.toLowerCase() == "null") {
            ""
        } else v
    } catch (e: Exception) {
    }
    return ""
}

fun List<String>.readLong(index: Int): Long {
    try {
        return this[index].toLong()
    } catch (e: Exception) {
    }
    return 0L
}

fun List<String>.readInt(index: Int): Int {
    try {
        return this[index].toInt()
    } catch (e: Exception) {
    }
    return 0
}

inline fun Call<TvsoriResponse>.post(crossinline callback: (isSucc: Boolean, rtnMsg: List<String>) -> Unit) {
    enqueue(object : Callback<TvsoriResponse> {
        override fun onResponse(call: Call<TvsoriResponse>, response: Response<TvsoriResponse>) {
            response.body()?.let { body ->
                Logger.log("METHOD = ${call.request()?.url()} / BODY = $body")
                callback(body.isSucc(), body.rtnMsg ?: emptyList())
                return
            }
            callback(false, emptyList())
        }

        override fun onFailure(call: Call<TvsoriResponse>, t: Throwable) {
            Logger.log("ERROR METHOD = ${call.request().url()}", t)
            callback(false, emptyList())
        }
    })
}

inline fun Call<TvsoriArrayResponse>.postArray(crossinline callback: (isSucc: Boolean, rtnMsg: List<List<String>>) -> Unit) {
    enqueue(object : Callback<TvsoriArrayResponse> {
        override fun onResponse(call: Call<TvsoriArrayResponse>, response: Response<TvsoriArrayResponse>) {
            response.body()?.let { body ->
                Logger.log("METHOD = ${call.request()?.url()} / BODY = $body")
                callback(true, body.rtnMsg ?: emptyList())
                return
            }
            callback(false, emptyList())
        }

        override fun onFailure(call: Call<TvsoriArrayResponse>, t: Throwable) {
            Logger.log("ERROR METHOD = ${call.request().url()}", t)
            callback(false, emptyList())
        }
    })
}

inline fun Call<String>.post(crossinline callback: (rtnMsg: List<AppBoardSongInfo>) -> Unit) {
    enqueue(object : Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            val song_list = mutableListOf<AppBoardSongInfo>()
            if (response.isSuccessful) {
                response.body()?.let { itbody ->
                    val message = itbody.split("\r".toRegex()).dropLastWhile { it.isEmpty() }.toList()
                    Logger.log("SONG RESULT $message")
                    if (message.readString(0).contains("true")) {
                        for (i in 1 until message.size) {
                            val msg = message.readString(i)
                            val msg_list = msg.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toList()
                            val data = AppBoardSongInfo(msg_list, true)
                            song_list.add(data)
                        }
                    }
                }
            }
            callback(song_list)
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            callback(emptyList())
        }
    })
}

/**
 * Boolean Read Write Delegate
 */
fun SharedPreferences.boolean(defaultValue: Boolean = false, key: String? = null): ReadWriteProperty<Any, Boolean> =
    delegate(defaultValue, key, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

/**
 * Float Read Write Delegate
 */
fun SharedPreferences.float(defaultValue: Float = 0f, key: String? = null): ReadWriteProperty<Any, Float> =
    delegate(defaultValue, key, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

/**
 * Int Read Write Delegate
 */
fun SharedPreferences.int(defaultValue: Int = 0, key: String? = null): ReadWriteProperty<Any, Int> =
    delegate(defaultValue, key, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

/**
 * Long Read Write Delegate
 */
fun SharedPreferences.long(defaultValue: Long = 0, key: String? = null): ReadWriteProperty<Any, Long> =
    delegate(defaultValue, key, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

/**
 * String Read Write Delegate
 */
fun SharedPreferences.string(defaultValue: String = "", key: String? = null): ReadWriteProperty<Any, String> =
    delegate(defaultValue, key, SharedPreferences::getString, SharedPreferences.Editor::putString)

/**
 * Nullable String Read Write Delegate
 */
fun SharedPreferences.nullableString(key: String? = null): ReadWriteProperty<Any, String?> =
    nullableDelegate("", key, SharedPreferences::getString, SharedPreferences.Editor::putString)

private inline fun <T : Any> SharedPreferences.delegate(
    defaultValue: T, key: String?,
    crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
    crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor
) = object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) = getter(key ?: property.name, defaultValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        edit().setter(key ?: property.name, value).apply()
}

private inline fun <T : Any> SharedPreferences.nullableDelegate(
    dummy: T, key: String?,
    crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
    crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor
) = object : ReadWriteProperty<Any, T?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val target = key ?: property.name
        return if (contains(target)) getter(target, dummy) else null
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        val target = key ?: property.name
        if (value == null) {
            edit().remove(target).apply()
        } else {
            edit().setter(target, value).apply()
        }
    }
}