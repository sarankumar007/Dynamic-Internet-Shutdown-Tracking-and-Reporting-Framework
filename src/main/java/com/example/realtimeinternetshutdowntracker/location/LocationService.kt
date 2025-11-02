package com.example.realtimeinternetshutdowntracker.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    suspend fun getCurrentLocation(): LocationResult? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    LocationResult(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        district = getDistrictFromCoordinates(it.latitude, it.longitude),
                        state = getStateFromCoordinates(it.latitude, it.longitude)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private suspend fun getDistrictFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.subAdminArea ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }
    
    private suspend fun getStateFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.adminArea ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }
}

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val district: String,
    val state: String
)
