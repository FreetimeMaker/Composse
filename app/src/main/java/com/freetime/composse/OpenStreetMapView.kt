package com.freetime.composse

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    onLocationUpdate: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var webView by remember { mutableStateOf<WebView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    
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
                    currentLocation = it
                    onLocationUpdate(it)
                    // Update map center
                    webView?.evaluateJavascript(
                        """
                        map.setView([${it.latitude}, ${it.longitude}], 15);
                        if (currentLocationMarker) {
                            currentLocationMarker.setLatLng([${it.latitude}, ${it.longitude}]);
                        } else {
                            currentLocationMarker = L.marker([${it.latitude}, ${it.longitude}], {
                                icon: L.divIcon({
                                    className: 'current-location-marker',
                                    html: '<div style="background: #2196F3; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
                                    iconSize: [20, 20]
                                })
                            }).addTo(map);
                        }
                        """.trimIndent(),
                        null
                    )
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    fun addMarker(title: String, geoPoint: GeoPoint) {
        webView?.evaluateJavascript(
            """
            L.marker([${geoPoint.latitude}, ${geoPoint.longitude}])
                .addTo(map)
                .bindPopup('$title');
            """.trimIndent(),
            null
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setGeolocationEnabled(true)
                    
                    webViewClient = WebViewClient()
                    
                    // Load OpenStreetMap with Leaflet.js
                    loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>OpenStreetMap</title>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                            <style>
                                body { margin: 0; padding: 0; }
                                #map { height: 100vh; width: 100vw; }
                                .current-location-marker { 
                                    background: #2196F3 !important; 
                                    border-radius: 50% !important;
                                }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                var map = L.map('map').setView([52.520008, 13.404954], 13);
                                var currentLocationMarker = null;
                                
                                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                    attribution: '© OpenStreetMap contributors',
                                    maxZoom: 19
                                }).addTo(map);
                                
                                // Add sample markers
                                L.marker([52.520008, 13.404954])
                                    .addTo(map)
                                    .bindPopup('Berlin');
                                    
                                L.marker([48.135125, 11.582080])
                                    .addTo(map)
                                    .bindPopup('Munich');
                            </script>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        FloatingActionButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_mylocation),
                contentDescription = "Current Location"
            )
        }
    }
    
    // Add markers when location is available
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            addMarker("Current Location", it)
        }
    }
}
