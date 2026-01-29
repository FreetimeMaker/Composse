package com.freetime.composse.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

@Composable
private fun rememberMapView(): MapView {
    val context = LocalContext.current
    return remember {
        MapView(context)
    }
}

@Composable
fun ComposeMap(
    modifier: Modifier = Modifier,
    style: Style.Builder,
    camera: CameraState? = null,
    markers: List<MapMarker> = emptyList(),
    polylines: List<MapPolyline> = emptyList(),
    onMapReady: (MapLibreMap) -> Unit = {}
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

                    map.removeAnnotations()

                    markers.forEach { marker ->
                        map.addMarker(
                            MarkerOptions()
                                .position(marker.position)
                                .title(marker.title)
                        )
                    }

                    polylines.forEach { poly ->
                        map.addPolyline(
                            PolylineOptions()
                                .addAll(poly.points)
                                .color(poly.color)
                                .width(poly.width)
                        )
                    }

                    onMapReady(map)
                }
            }
        }
    )
}
