package com.freetime.composse

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.MapPosition
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.datastore.MultiMapDataStore
import org.mapsforge.map.datastore.SingleMapDataStore
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.ExternalRenderTheme
import java.io.File

class MapsforgeManager(private val context: Context) {
    
    private val offlineMapsDir = File(context.getExternalFilesDir(null), "offline_maps")
    
    init {
        AndroidGraphicFactory.createInstance(context.applicationContext)
        if (!offlineMapsDir.exists()) {
            offlineMapsDir.mkdirs()
        }
    }
    
    suspend fun createOfflineMapView(): MapView? = withContext(Dispatchers.Main) {
        try {
            val mapView = MapView(context)
            val graphicFactory: GraphicFactory = AndroidGraphicFactory.INSTANCE
            
            // Create tile cache
            val tileCache = createTileCache(mapView, graphicFactory)
            
            // Load available offline maps
            val mapDataStore = loadOfflineMaps()
            
            if (mapDataStore != null) {
                // Create tile renderer layer
                val tileRendererLayer = TileRendererLayer(
                    tileCache,
                    mapDataStore,
                    mapView.model.mapViewPosition,
                    false,
                    true,
                    false,
                    graphicFactory
                )
                
                // Set render theme
                val themeFile = getRenderThemeFile()
                if (themeFile.exists()) {
                    tileRendererLayer.setXmlRenderTheme(ExternalRenderTheme(themeFile.absolutePath))
                }
                
                // Add layer to map view
                mapView.layerManager.layers.add(tileRendererLayer)
                
                // Set initial position
                val boundingBox = mapDataStore.boundingBox()
                val center = LatLong(
                    (boundingBox.maxLatitude + boundingBox.minLatitude) / 2,
                    (boundingBox.maxLongitude + boundingBox.minLongitude) / 2
                )
                mapView.model.mapViewPosition.setMapPosition(
                    MapPosition(center, (12).toByte())
                )
                
                mapView
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun createTileCache(mapView: MapView, graphicFactory: GraphicFactory): TileCache {
        val tileSize = 256
        val cacheSize = 100 // Number of tiles in cache
        return org.mapsforge.map.layer.cache.TileCache.createTileCache(
            mapView.model.displayModel.tileSize,
            mapView.model.displayModel.tileSize,
            cacheSize,
            mapView.model.displayModel.tileSize
        )
    }
    
    private suspend fun loadOfflineMaps(): MapDataStore? = withContext(Dispatchers.IO) {
        try {
            val mapFiles = offlineMapsDir.listFiles { file ->
                file.name.endsWith(".map") || file.name.endsWith(".map.gz")
            }
            
            if (mapFiles?.isNotEmpty() == true) {
                val mapDataStores = mutableListOf<MapDataStore>()
                
                mapFiles.forEach { mapFile ->
                    try {
                        val mapDataStore = SingleMapDataStore(
                            MapFile(mapFile, 0, false, false)
                        )
                        mapDataStores.add(mapDataStore)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                if (mapDataStores.isNotEmpty()) {
                    if (mapDataStores.size == 1) {
                        mapDataStores.first()
                    } else {
                        MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL)
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getRenderThemeFile(): File {
        // Copy default render theme from assets if not exists
        val themeFile = File(offlineMapsDir, "default.xml")
        if (!themeFile.exists()) {
            try {
                context.assets.open("mapsforge/default.xml").use { input ->
                    themeFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return themeFile
    }
    
    suspend fun downloadOfflineMap(
        regionName: String,
        downloadUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val mapFile = File(offlineMapsDir, "$regionName.map")
            
            // In a real implementation, you would download from a Mapsforge tile server
            // For now, we'll create a placeholder
            mapFile.writeText("Mapsforge map file for $regionName")
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getAvailableOfflineMaps(): List<String> {
        return offlineMapsDir.listFiles { file ->
            file.name.endsWith(".map") || file.name.endsWith(".map.gz")
        }?.map { it.nameWithoutExtension } ?: emptyList()
    }
    
    fun getOfflineMapSize(): Long {
        return offlineMapsDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    fun deleteOfflineMap(regionName: String): Boolean {
        val mapFile = File(offlineMapsDir, "$regionName.map")
        return if (mapFile.exists()) {
            mapFile.delete()
        } else {
            false
        }
    }
}
