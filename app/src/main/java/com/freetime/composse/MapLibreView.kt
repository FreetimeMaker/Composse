package com.freetime.composse

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onLocationUpdate: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var currentLocationSymbol by remember { mutableStateOf<Symbol?>(null) }
    
    // Initialize MapLibre
    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun getCurrentLocation() {
        if (locationManager.hasLocationPermission()) {
            coroutineScope.launch {
                val location = locationManager.getCurrentLocation()
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    onLocationUpdate(latLng)
                    
                    mapLibreMap?.let { map ->
                        // Move camera to current location
                        val cameraPosition = CameraPosition.Builder()
                            .target(latLng)
                            .zoom(15.0)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        
                        // Update or create current location marker
                        currentLocationSymbol?.let { symbol ->
                            symbolManager?.delete(symbol)
                        }
                        
                        symbolManager?.create(
                            SymbolOptions()
                                .withPosition(latLng)
                                .withIconImage("current-location-icon")
                                .withIconSize(1.5f)
                                .withIconAnchor("center")
                        )?.let { symbol ->
                            currentLocationSymbol = symbol
                        }
                    }
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    fun addMarker(title: String, latLng: LatLng) {
        symbolManager?.create(
            SymbolOptions()
                .withPosition(latLng)
                .withIconImage("poi-icon")
                .withIconSize(1.2f)
                .withIconAnchor("bottom")
                .withTextField(title)
        )
    }
    
    fun changeMapStyle(styleUrl: String) {
        mapLibreMap?.setStyle(Style.Builder().fromUri(styleUrl))
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    
                    getMapAsync(object : OnMapReadyCallback {
                        override fun onMapReady(map: MapLibreMap) {
                            mapLibreMap = map
                            
                            // Set initial style (MapLibre Streets)
                            map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
                                // Add custom icons
                                style.addImage(
                                    "current-location-icon",
                                    createLocationIcon()
                                )
                                style.addImage(
                                    "poi-icon",
                                    createPOIIcon()
                                )
                                
                                // Initialize symbol manager
                                symbolManager = SymbolManager(this@apply, map, style)
                                symbolManager?.iconAllowOverlap = true
                                symbolManager?.textAllowOverlap = true
                                
                                // Add sample markers
                                addMarker("Berlin", LatLng(52.520008, 13.404954))
                                addMarker("Brandenburg Gate", LatLng(52.516275, 13.377704))
                                addMarker("Museum Island", LatLng(52.524268, 13.406290))
                                
                                // Get current location
                                getCurrentLocation()
                            }
                            
                            // Enable location component
                            map.uiSettings.isCompassEnabled = true
                            map.uiSettings.isZoomGesturesEnabled = true
                            map.uiSettings.isScrollGesturesEnabled = true
                            map.uiSettings.isRotateGesturesEnabled = true
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // GPS Button
        FloatingActionButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_mylocation),
                contentDescription = "Current Location"
            )
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

// Helper functions for creating icons (simplified)
private fun createLocationIcon(): android.graphics.Bitmap {
    val size = 32
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLUE
        isAntiAlias = true
    }
    canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 3).toFloat(), paint)
    return bitmap
}

private fun createPOIIcon(): android.graphics.Bitmap {
    val size = 24
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF5722")
        isAntiAlias = true
    }
    canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 3).toFloat(), paint)
    return bitmap
}
