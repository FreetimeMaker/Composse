package com.freetime.composse.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.freetime.composse.ui.map.BRouterWrapper
import com.freetime.composse.ui.map.MapState
import com.freetime.composse.ui.map.MapViewWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.io.File

@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val mapState = remember { MapState() }
    val coroutineScope = rememberCoroutineScope()

    // Replace with your actual BRouter data path
    val brouterDataPath = File(context.getExternalFilesDir(null), "brouter/segments4").absolutePath
    if (!File(brouterDataPath).exists()) {
        // Handle case where BRouter data is not available.
        // You might want to show a message to the user.
    }


    Box(modifier = Modifier.fillMaxSize()) {
        MapViewWrapper(mapState)
    }

    // Example of how to calculate a route.
    // In a real app, this should be triggered by a user action and handled in a ViewModel.
    LaunchedEffect(mapState.map) {
        if (mapState.map != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val start = LatLng(52.52, 13.40) // Example start point
                val end = LatLng(52.53, 13.41)   // Example end point
                val route = BRouterWrapper.calculateRoute(start, end, brouterDataPath, "trekking")
                mapState.route = route
            }
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
