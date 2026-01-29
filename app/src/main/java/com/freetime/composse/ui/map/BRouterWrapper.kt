package com.freetime.composse.ui.map

import org.maplibre.android.geometry.LatLng
import java.io.File
import btools.router.BRouter

object BRouterWrapper {

    /**
     * Calculates a route between two points using BRouter.
     *
     * Note: This is a blocking call and should be run on a background thread.
     *
     * @param start The starting point of the route.
     * @param end The ending point of the route.
     * @param brouterDataPath The absolute path to the directory containing BRouter segment files (e.g., /.../brouter/segments4).
     * @param profile The routing profile to use (e.g., "trekking", "fastbike", "car-fast").
     * @return A list of LatLng points representing the route, or an empty list if a route could not be found.
     */
    fun calculateRoute(start: LatLng, end: LatLng, brouterDataPath: String, profile: String): List<LatLng> {
        val router = BRouter(brouterDataPath)

        return try {
            val track = router.getTrack(profile, doubleArrayOf(start.longitude, start.latitude), doubleArrayOf(end.longitude, end.latitude))

            // Convert the BRouter track to a list of LatLng
            track?.nodes?.map { LatLng(it.lat / 1000000.0, it.lon / 1000000.0) } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
