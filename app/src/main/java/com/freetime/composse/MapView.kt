package com.freetime.composse

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    onLocationUpdate: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val offlineMapManager = remember { OfflineMapManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var customOverlay by remember { mutableStateOf<CustomOverlay?>(null) }
    
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
                    onLocationUpdate(it)
                    mapView?.controller?.animateTo(it)
                    customOverlay?.addCurrentLocationMarker(it)
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    LaunchedEffect(mapView) {
        mapView?.let { map ->
            customOverlay = CustomOverlay(map)
            offlineMapManager.enableOfflineMode(map)
            
            // Add some sample markers
            customOverlay?.addMarker("Berlin", GeoPoint(52.520008, 13.404954))
            customOverlay?.addMarker("Munich", GeoPoint(48.135125, 11.582080))
            
            // Get current location
            getCurrentLocation()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(52.520008, 13.404954))
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        FloatingActionButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_mylocation),
                contentDescription = "Current Location"
            )
        }
    }
}
