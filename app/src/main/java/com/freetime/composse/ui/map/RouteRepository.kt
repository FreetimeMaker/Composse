package com.freetime.composse.ui.map

import com.freetime.composse.ui.map.model.RouteResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.maplibre.android.geometry.LatLng

class RouteRepository(private val client: OkHttpClient, private val json: Json) {

    fun getRoute(start: LatLng, end: LatLng): List<LatLng> {
        // Replace with your actual routing API endpoint
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=YOUR_API_KEY&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val routeResponse = response.body?.string()?.let {
                        json.decodeFromString<RouteResponse>(it)
                    }
                    return routeResponse?.features?.firstOrNull()?.geometry?.coordinates?.map {
                        LatLng(it[1], it[0])
                    } ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}
