package com.freetime.composse

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import java.io.File

class OfflineMapManager(private val context: Context) {
    
    private val offlineCacheDir = File(context.cacheDir, "osmdroid")
    
    init {
        // Configure offline cache
        Configuration.getInstance().osmdroidTileCache = offlineCacheDir
    }
    
    suspend fun enableOfflineMode(mapView: MapView) = withContext(Dispatchers.IO) {
        try {
            // Use cached tiles when offline
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            
            // Enable tile caching
            val tileWriter = SqlTileWriter()
            tileWriter.setCacheSize(100 * 1024 * 1024) // 100MB cache
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun preloadArea(
        mapView: MapView,
        north: Double,
        south: Double,
        east: Double,
        west: Double,
        zoomMin: Int = 10,
        zoomMax: Int = 16
    ) = withContext(Dispatchers.IO) {
        try {
            // This would download tiles for the specified area
            // Implementation depends on specific offline requirements
            val tileWriter = SqlTileWriter()
            
            for (zoom in zoomMin..zoomMax) {
                // Download tiles logic here
                // This is a simplified version
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getCacheSize(): Long {
        return offlineCacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    fun clearCache() {
        offlineCacheDir.deleteRecursively()
        offlineCacheDir.mkdirs()
    }
}
