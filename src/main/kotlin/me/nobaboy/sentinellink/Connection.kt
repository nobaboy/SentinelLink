package me.nobaboy.sentinellink

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Connection {
//    private const val HOST: String = "https://sentinel.celestialfault.dev/"
    private const val HOST: String = "http://localhost:8888/"

    data class ResponseInfo(val responseCode: Int, val responseMessage: String)

    fun attemptAcknowledge(uuid: String, token: String?) {
        if (token == null) {
            SentinelLink.send("Your token is invalid; get a new token from the Discord server.")
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("account_uuid", uuid)
            addProperty("token", token)
        }
        val json = Gson().toJson(jsonObject)

        var conn: HttpURLConnection? = null
        var responseInfo: ResponseInfo

        try {
            val url = URL(HOST)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            conn.outputStream.writer().use {
                it.write(json)
                it.flush()
            }

            val responseMessage = if (conn.responseCode == 200) {
                conn.inputStream.reader().readText()
            } else {
                conn.errorStream?.reader()?.readText() ?: "No response message."
            }
            responseInfo = ResponseInfo(conn.responseCode, responseMessage)
        } catch (e: IOException) {
            SentinelLink.LOGGER.error("Error connecting to server: ${e.message}")
            responseInfo = ResponseInfo(HttpURLConnection.HTTP_INTERNAL_ERROR, "Connection failed, try again later!")
        } finally {
            conn?.disconnect()
        }

        processResponse(responseInfo)
    }

    private fun processResponse(responseInfo: ResponseInfo) {
        when (responseInfo.responseCode) {
            200, 400, 401, 403, 404, 500 -> {
                SentinelLink.send(responseInfo.responseMessage)
            }
            else -> {
                SentinelLink.send("Received invalid response code from server. Code: ${responseInfo.responseCode}")
            }
        }
    }
}