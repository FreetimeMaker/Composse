package com.freetime.composse.ui.map.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotonResponse(
    val features: List<PhotonFeature>,
    val type: String
)

@Serializable
data class PhotonFeature(
    val geometry: PhotonGeometry,
    val type: String,
    val properties: PhotonProperties
)

@Serializable
data class PhotonGeometry(
    val coordinates: List<Double>,
    val type: String
)

@Serializable
data class PhotonProperties(
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val state: String? = null,
    val osm_id: Long? = null,
    val osm_type: String? = null,
    val osm_key: String? = null,
    val osm_value: String? = null
) {
    fun getDisplayName(): String {
        val parts = listOfNotNull(name, street, housenumber, city, country)
        return parts.joinToString(", ")
    }
}
