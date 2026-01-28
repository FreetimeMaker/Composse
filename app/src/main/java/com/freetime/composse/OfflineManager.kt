package com.freetime.composse

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OfflineManager(private val context: Context) {
    
    private val cacheDir = File(context.cacheDir, "maps_cache")
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    suspend fun downloadOfflineMap(
        regionName: String,
        north: Double,
        south: Double,
        east: Double,
        west: Double,
        minZoom: Int = 8,
        maxZoom: Int = 16
    ) = withContext(Dispatchers.IO) {
        try {
            val regionDir = File(cacheDir, regionName.replace(" ", "_"))
            if (!regionDir.exists()) {
                regionDir.mkdirs()
            }
            
            // This is a simplified version - in reality you'd download tiles
            // For now, we'll create a placeholder file
            val infoFile = File(regionDir, "region_info.txt")
            infoFile.writeText("""
                Region: $regionName
                Bounds: $north,$south,$east,$west
                Zoom: $minZoom-$maxZoom
                Downloaded: ${System.currentTimeMillis()}
            """.trimIndent())
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getDownloadedRegions(): List<String> {
        return cacheDir.listFiles()?.map { it.name } ?: emptyList()
    }
    
    fun getCacheSize(): Long {
        return cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    fun clearCache() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }
}
