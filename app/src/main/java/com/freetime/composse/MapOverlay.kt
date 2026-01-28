package com.freetime.composse

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Marker

class CustomOverlay(private val mapView: MapView) : Overlay() {
    
    private val markers = mutableListOf<Marker>()
    
    fun addMarker(title: String, geoPoint: org.osmdroid.util.GeoPoint) {
        val marker = Marker(mapView)
        marker.title = title
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        markers.add(marker)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }
    
    fun addCurrentLocationMarker(geoPoint: org.osmdroid.util.GeoPoint) {
        // Remove existing current location marker
        markers.filter { it.title == "Current Location" }.forEach {
            mapView.overlays.remove(it)
        }
        markers.removeAll { it.title == "Current Location" }
        
        val marker = Marker(mapView)
        marker.title = "Current Location"
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        
        // Set blue icon for current location
        marker.setIcon(mapView.context.getDrawable(android.R.drawable.ic_menu_mylocation))
        
        markers.add(marker)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }
    
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        // Custom drawing if needed
    }
}
