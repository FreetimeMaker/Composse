package com.freetime.composse.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.freetime.composse.ui.map.*
import com.mapbox.mapboxsdk.geometry.LatLng

@Composable
fun MapScreen() {

    var camera by remember {
        mutableStateOf(
            CameraState(
                target = LatLng(47.39, 8.08),
                zoom = 12.0
            )
        )
    }

    val markers = listOf(
        MapMarker(
            position = LatLng(47.39, 8.08),
            title = "Hello Switzerland"
        )
    )

    ComposeMap(
        modifier = Modifier.fillMaxSize(),
        style = osmRasterStyle(),
        camera = camera,
        markers = markers
    )
}
