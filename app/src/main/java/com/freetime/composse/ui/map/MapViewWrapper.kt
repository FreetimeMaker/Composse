package com.freetime.composse.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions

@Composable
fun MapViewWrapper(mapState: MapState) {
    val context = LocalContext.current

    val mapView = remember {
        val mv = MapView(context)
        mapState.mapView = mv
        mv
    }

    AndroidView({ mapView }) {
        it.getMapAsync { map ->
            MapLibre.getInstance(context)
            map.setStyle(Style.getPredefinedStyle("Streets")) { style ->
                mapState.onMapReady(map)
            }
        }
    }

    LaunchedEffect(mapState.route) {
        val map = mapState.map
        val mv = mapState.mapView
        if (map != null && mv != null && mapState.route.isNotEmpty()) {
            map.style?.let { style ->
                val lineManager = LineManager(mv, map, style)
                lineManager.deleteAll()
                val lineOptions = LineOptions()
                    .withLatLngs(mapState.route)
                    .withLineColor("#FF0000") // Red color
                    .withLineWidth(5.0f)
                lineManager.create(lineOptions)

                val latLngBounds = LatLngBounds.Builder()
                    .includes(mapState.route)
                    .build()
                map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 1000)
            }
        }
    }
}
