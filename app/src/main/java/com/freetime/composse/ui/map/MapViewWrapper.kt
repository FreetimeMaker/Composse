package com.freetime.composse.ui.map

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.mapbox.mapboxsdk.maps.MapView

@Composable
fun rememberMapView(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}
