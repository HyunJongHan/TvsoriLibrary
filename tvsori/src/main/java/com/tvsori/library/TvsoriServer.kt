package com.tvsori.library

import android.os.AsyncTask
import android.util.Log
import com.tvsori.library.enums.InCmd
import com.tvsori.library.enums.OutCmd
import com.tvsori.library.enums.ServerErrorCode
import com.tvsori.library.util.Logger
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.util.*

class TvsoriServer(private var coonectCallback: ((Boolean, Boolean) -> Unit)?, private var msgCallback: ((InCmd, ServerErrorCode, List<String>) -> Unit)?) : Thread() {
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var tempArray: ByteArray? = null

    private var isConnected = false

    fun sendMessage(cmd: OutCmd, message: Array<String?>) {
        outputStream?.let { stream ->
            try {
                stream.write(byteArrayOf(-1, -1))
                stream.write(String.format("%2d", cmd.ordinal).toByteArray())
                stream.write(byteArrayOf(-1))
                stream.write("0".toByteArray())
                stream.write(byteArrayOf(-1))
                for (msg in message) {
                    msg?.let { m ->
                        stream.write(m.toByteArray())
                        stream.write(byteArrayOf(-1))
                    }
                }
                stream.write(byteArrayOf(-1))
                stream.flush()
            } catch (e: Exception) {
            }
        }
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    fun doLogout(userId: String) {
        doLogoutAsync(userId)
    }

    override fun run() {
        super.run()
        try {
            socket = Socket(InetAddress.getByName(TvsoriConstants.serverSocketIp), TvsoriConstants.serverSocketPort).also {
                inputStream = it.getInputStream()
                outputStream = it.getOutputStream()
            }
        } catch (e: Exception) {
            coonectCallback?.invoke(false, true)
            closeThread()
            return
        }
        coonectCallback?.invoke(true, false)
        isConnected = true
        try {
            do {
                val data = ByteArray(4096)
                inputStream?.run {
                    val length = read(data)
                    if (length > 0) {
                        parseServerData(data, length)
                    }
                }
            } while (!isInterrupted)
        } catch (e: Exception) {
            Log.e("TEST", "INPUT ERROR", e)
        }
        coonectCallback?.invoke(false, false)
        closeThread()
    }

    private fun closeThread() {
        Log.e("TEST", "CLOSE THREAD")
        try {
            inputStream?.close()
            inputStream = null
        } catch (e: Exception) {
        }

        try {
            outputStream?.close()
            outputStream = null
        } catch (e: Exception) {
        }

        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
        }
        isConnected = false
    }

    @Synchronized
    private fun parseServerData(serverdata: ByteArray, _length: Int) {
        val length = _length + (tempArray?.size ?: 0)
        var data = byteArrayOf()

        tempArray?.let { temparray ->
            data += temparray
            tempArray = null
        }
        data += serverdata.sliceArray(0 until _length)
        val serv_msg = mutableListOf<ByteArray>()
        var pos = 0
        var i = 0
        while (i < data.size) {
            if (i < data.size - 1 && data[i].toInt() == -1 && data[i + 1].toInt() == -1) {
                serv_msg.add(Arrays.copyOfRange(data, pos, i))
                pos = i + 2
                i++
            }
            i++
        }
        if (pos < length - 1) {
            tempArray = data.sliceArray(pos until length)
        }
        for (_msg in serv_msg) {
            val temp_list = mutableListOf<String>()
            pos = 0
            for (ii in _msg.indices) {
                if (_msg[ii].toInt() == -1) {
                    temp_list.add(String(_msg, pos, ii - pos))
                    pos = ii + 1
                }
            }
            if (pos < _msg.size) {
                temp_list.add(String(_msg, pos, _msg.size - pos))
            }
            if (temp_list.size > 0) {
                if (temp_list.size < 2) {
                    return
                }

                val msg = temp_list.slice(2 until temp_list.size)
                val cmd = InCmd.fromOrdinal(temp_list.readInt(0))
                val errCode = ServerErrorCode.fromValue(msg.readInt(0))

                if (errCode === ServerErrorCode.RERR_NONE) {
                    if (cmd != InCmd.ROOMINFO && cmd != InCmd.USERINFO)
                        Logger.log("RECEIVE MESSAGE CMD = $cmd / MESSAGE = $msg")
                } else {
                    Logger.log("RECEIVE MESSAGE CMD = $cmd / ERROR = $errCode /  MESSAGE = $msg")
                }
                if (cmd === InCmd.DATA) {
                    Logger.log("RECEIVE MESSAGE FAIL *************** CMD = $cmd / MESSAGE = $temp_list")
                }
                msgCallback?.invoke(cmd, errCode, msg)
            }
        }
    }

    inner class doLogoutAsync(private val userId: String) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            sendMessage(OutCmd.LOGOUT, arrayOf(userId))
            return null
        }
    }
}