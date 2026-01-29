package com.freetime.composse.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng

class MapState {
    var mapView: MapView? by mutableStateOf(null)
    var map: MapLibreMap? by mutableStateOf(null)
    var route: List<LatLng> by mutableStateOf(emptyList())

    fun onMapReady(map: MapLibreMap) {
        this.map = map
    }
}
