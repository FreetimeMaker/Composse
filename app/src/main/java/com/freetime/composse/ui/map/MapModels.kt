package com.freetime.composse.ui.map

import com.mapbox.mapboxsdk.geometry.LatLng

data class CameraState(
    val target: LatLng,
    val zoom: Double
)

data class MapMarker(
    val position: LatLng,
    val title: String? = null
)
