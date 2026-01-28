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
import org.maplibre.android.geometry.LatLng
import org.mapsforge.map.android.view.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HybridMapApp() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val mapsforgeManager = remember { MapsforgeManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var showOfflineMenu by remember { mutableStateOf(false) }
    var selectedMapType by remember { mutableStateOf("online") } // "online" or "offline"
    var selectedStyle by remember { mutableStateOf("streets") }
    
    var mapLibreView by remember { mutableStateOf<android.view.View?>(null) }
    var mapsforgeView by remember { mutableStateOf<MapView?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Location will be handled by map views
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun switchMapType(type: String) {
        selectedMapType = type
        // Map views will be recreated in recomposition
    }
    
    fun searchLocation(query: String) {
        // Search functionality would use Nominatim API
        // For now, this is a placeholder
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Map View - Switch between Online and Offline
        if (selectedMapType == "online") {
            // MapLibre GL (Online)
            MapLibreView(
                modifier = Modifier.fillMaxSize(),
                onLocationUpdate = { location ->
                    currentLocation = location
                }
            )
        } else {
            // Mapsforge (Offline)
            AndroidView(
                factory = { ctx ->
                    coroutineScope.launch {
                        mapsforgeView = mapsforgeManager.createOfflineMapView()
                    }
                    FrameLayout(ctx).apply {
                        // Placeholder until Mapsforge view is ready
                        setBackgroundColor(Color.LightGray.toArgb())
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
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
                        text = "Map Type & Style",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Map Type Selection
                    Text("Map Source:", fontWeight = FontWeight.Medium)
                    listOf("online" to "Online (MapLibre)", "offline" to "Offline (Mapsforge)").forEach { (key, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMapType == key,
                                onClick = {
                                    switchMapType(key)
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Online Map Styles (only show when online)
                    if (selectedMapType == "online") {
                        Text("Online Style:", fontWeight = FontWeight.Medium)
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
        
        // Offline Maps Menu
        if (showOfflineMenu) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Offline Maps",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val availableMaps = mapsforgeManager.getAvailableOfflineMaps()
                    
                    if (availableMaps.isEmpty()) {
                        Text("No offline maps downloaded", color = Color.Gray)
                        TextButton(
                            onClick = { 
                                // TODO: Implement map download
                                showOfflineMenu = false
                            }
                        ) {
                            Text("Download Maps")
                        }
                    } else {
                        availableMaps.forEach { mapName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(mapName, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        mapsforgeManager.deleteOfflineMap(mapName)
                                        showOfflineMenu = false
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "Storage: ${mapsforgeManager.getOfflineMapSize() / (1024 * 1024)} MB",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    TextButton(
                        onClick = { showOfflineMenu = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
        
        // Bottom Navigation Bar (Enhanced for Hybrid)
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
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Search Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { showSearch = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Text("Search", fontSize = 9.sp)
                }
                
                // Layers Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { showLayerMenu = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Layers")
                    }
                    Text("Type", fontSize = 9.sp)
                }
                
                // Offline Maps Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { showOfflineMenu = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Offline")
                    }
                    Text("Offline", fontSize = 9.sp)
                }
                
                // GPS Button (Centered, larger)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = { 
                            if (locationManager.hasLocationPermission()) {
                                // Location update handled by map views
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.size(52.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = Color.White
                        )
                    }
                    Text("GPS", fontSize = 9.sp)
                }
                
                // Bookmarks Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement bookmarks */ },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                    }
                    Text("Saved", fontSize = 9.sp)
                }
                
                // Settings Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement settings */ },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Text("Settings", fontSize = 9.sp)
                }
            }
        }
    }
}
