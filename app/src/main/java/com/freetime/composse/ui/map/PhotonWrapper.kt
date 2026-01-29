package com.freetime.composse.ui.map

import com.freetime.composse.ui.map.model.PhotonResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object PhotonWrapper {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun search(query: String, callback: (PhotonResponse?) -> Unit) {
        val url = "https://photon.komoot.io/api/?q=$query"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    return
                }

                response.body?.string()?.let {
                    try {
                        val photonResponse = json.decodeFromString<PhotonResponse>(it)
                        callback(photonResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                }
            }
        })
    }
}
