package com.freetime.composse

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMapApp() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf("streets") }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun searchLocation(query: String) {
        // Placeholder for search functionality
        Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Simple Map View (using WebView as fallback)
        AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    
                    loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Simple Map</title>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                            <style>
                                body { margin: 0; padding: 0; }
                                #map { height: 100vh; width: 100vw; }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                var map = L.map('map').setView([52.520008, 13.404954], 13);
                                
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
                        text = "Map Styles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val styles = mapOf(
                        "streets" to "Streets",
                        "satellite" to "Satellite",
                        "outdoors" to "Outdoors",
                        "light" to "Light",
                        "dark" to "Dark"
                    )
                    
                    styles.forEach { (key, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStyle == key,
                                onClick = {
                                    selectedStyle = key
                                    showLayerMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
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
                        Icon(Icons.Default.Menu, contentDescription = "Layers")
                    }
                    Text("Styles", fontSize = 10.sp)
                }
                
                // GPS Button (Centered, larger)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = { 
                            if (locationManager.hasLocationPermission()) {
                                coroutineScope.launch {
                                    val location = locationManager.getCurrentLocation()
                                    location?.let {
                                        currentLocation = it
                                        Toast.makeText(context, "Location: ${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
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
                        Icon(Icons.Default.Star, contentDescription = "Bookmarks")
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
}
