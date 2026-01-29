package com.freetime.composse.ui.map.model

import kotlinx.serialization.Serializable

@Serializable
data class RouteResponse(
    val features: List<Feature>
)

@Serializable
data class Feature(
    val geometry: Geometry
)

@Serializable
data class Geometry(
    val coordinates: List<List<Double>>
)
