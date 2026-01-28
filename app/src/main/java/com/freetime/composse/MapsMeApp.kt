package com.freetime.composse

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMeApp() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var webView by remember { mutableStateOf<WebView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableStateOf("standard") }
    
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
                    webView?.evaluateJavascript(
                        """
                        map.setView([${it.latitude}, ${it.longitude}], 15);
                        if (currentLocationMarker) {
                            currentLocationMarker.setLatLng([${it.latitude}, ${it.longitude}]);
                        } else {
                            currentLocationMarker = L.marker([${it.latitude}, ${it.longitude}], {
                                icon: L.divIcon({
                                    className: 'current-location-marker',
                                    html: '<div style="background: #2196F3; width: 16px; height: 16px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
                                    iconSize: [16, 16]
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
    
    fun changeMapLayer(layer: String) {
        selectedLayer = layer
        val tileUrl = when (layer) {
            "satellite" -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            "terrain" -> "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png"
            else -> "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        }
        
        webView?.evaluateJavascript(
            """
            if (currentTileLayer) {
                map.removeLayer(currentTileLayer);
            }
            currentTileLayer = L.tileLayer('$tileUrl', {
                attribution: '© OpenStreetMap contributors',
                maxZoom: 19
            }).addTo(map);
            """.trimIndent(),
            null
        )
    }
    
    fun searchLocation(query: String) {
        webView?.evaluateJavascript(
            """
            fetch('https://nominatim.openstreetmap.org/search?format=json&q=${query.replace("'", "%27")}')
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        const result = data[0];
                        const lat = parseFloat(result.lat);
                        const lon = parseFloat(result.lon);
                        map.setView([lat, lon], 16);
                        L.marker([lat, lon])
                            .addTo(map)
                            .bindPopup(result.display_name)
                            .openPopup();
                    }
                });
            """.trimIndent(),
            null
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Map View
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setGeolocationEnabled(true)
                    
                    webViewClient = WebViewClient()
                    
                    loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Maps.me Open Source</title>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                            <style>
                                body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                                #map { height: 100vh; width: 100vw; }
                                .current-location-marker { 
                                    background: #2196F3 !important; 
                                    border-radius: 50% !important;
                                }
                                .leaflet-popup-content-wrapper {
                                    border-radius: 8px;
                                    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                                }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                var map = L.map('map').setView([52.520008, 13.404954], 13);
                                var currentLocationMarker = null;
                                var currentTileLayer = null;
                                
                                currentTileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                    attribution: '© OpenStreetMap contributors',
                                    maxZoom: 19
                                }).addTo(map);
                                
                                // Add sample POIs
                                var pois = [
                                    {lat: 52.520008, lng: 13.404954, name: "Berlin", type: "city"},
                                    {lat: 52.516275, lng: 13.377704, name: "Brandenburg Gate", type: "landmark"},
                                    {lat: 52.524268, lng: 13.406290, name: "Museum Island", type: "museum"},
                                    {lat: 48.135125, lng: 11.582080, name: "Munich", type: "city"}
                                ];
                                
                                pois.forEach(function(poi) {
                                    var icon = L.divIcon({
                                        className: 'custom-marker',
                                        html: '<div style="background: #FF5722; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; box-shadow: 0 1px 3px rgba(0,0,0,0.3);"></div>',
                                        iconSize: [12, 12]
                                    });
                                    
                                    L.marker([poi.lat, poi.lng], {icon: icon})
                                        .addTo(map)
                                        .bindPopup('<strong>' + poi.name + '</strong><br><small>' + poi.type + '</small>');
                                });
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
        
        // Top Search Bar
        if (showSearch) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showSearch = false }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search location...") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = { 
                            if (searchQuery.isNotEmpty()) {
                                searchLocation(searchQuery)
                                showSearch = false
                                searchQuery = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        }
        
        // Layer Menu
        if (showLayerMenu) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Map Layers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    listOf("standard", "satellite", "terrain").forEach { layer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLayer == layer,
                                onClick = {
                                    changeMapLayer(layer)
                                    showLayerMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = layer.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = { showLayerMenu = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
        
        // Bottom Navigation Bar (Maps.me style)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Search Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { showSearch = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Text("Search", fontSize = 10.sp)
                }
                
                // Layers Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { showLayerMenu = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Layers")
                    }
                    Text("Layers", fontSize = 10.sp)
                }
                
                // GPS Button (Centered, larger)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = { getCurrentLocation() },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = Color.White
                        )
                    }
                    Text("GPS", fontSize = 10.sp)
                }
                
                // Bookmarks Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement bookmarks */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                    }
                    Text("Saved", fontSize = 10.sp)
                }
                
                // Settings Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement settings */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Text("Settings", fontSize = 10.sp)
                }
            }
        }
    }
    
    // Initialize
    LaunchedEffect(Unit) {
        getCurrentLocation()
    }
}
