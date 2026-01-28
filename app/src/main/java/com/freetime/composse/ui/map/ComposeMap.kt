package com.freetime.composse.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

@Composable
fun ComposeMap(
    modifier: Modifier = Modifier,
    style: Style.Builder,
    camera: CameraState? = null,
    markers: List<MapMarker> = emptyList(),
    onMapReady: (MapboxMap) -> Unit = {}
) {
    val mapView = rememberMapView()

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(style) {

                    camera?.let {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                it.target,
                                it.zoom
                            )
                        )
                    }

                    markers.forEach { marker ->
                        map.addMarker(
                            com.mapbox.mapboxsdk.annotations.MarkerOptions()
                                .position(marker.position)
                                .title(marker.title)
                        )
                    }

                    onMapReady(map)
                }
            }
        }
    )
}
