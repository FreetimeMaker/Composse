package com.freetime.composse.ui.map

import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet

fun osmRasterStyle(): Style.Builder {
    return Style.Builder()
        .fromUri("mapbox://styles/mapbox/empty-v9")
        .withSource(
            RasterSource(
                "osm",
                TileSet(
                    "tileset",
                    "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                ),
                256
            )
        )
        .withLayer(RasterLayer("osm-layer", "osm"))
}
