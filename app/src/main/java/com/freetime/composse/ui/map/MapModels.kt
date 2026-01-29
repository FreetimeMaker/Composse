package com.freetime.composse.ui.map

import org.maplibre.android.geometry.LatLng

data class CameraState(
    val target: LatLng,
    val zoom: Double
)

data class MapMarker(
    val position: LatLng,
    val title: String? = null
)

data class MapPolyline(
    val points: List<LatLng>,
    val color: Int,
    val width: Float
)
