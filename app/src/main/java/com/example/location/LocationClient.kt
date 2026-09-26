package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

data class UserLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationClient(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): UserLocationData? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                UserLocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    bearing = location.bearing,
                    speed = location.speed,
                    timestamp = location.time
                )
            } else {
                val lastLoc = fusedLocationClient.lastLocation.await()
                lastLoc?.let {
                    UserLocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        bearing = it.bearing,
                        speed = it.speed,
                        timestamp = it.time
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 2000L): Flow<UserLocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMinUpdateDistanceMeters(0.5f)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(
                        UserLocationData(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracy = loc.accuracy,
                            bearing = loc.bearing,
                            speed = loc.speed,
                            timestamp = loc.time
                        )
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    companion object {
        fun calculateDistanceMeters(
            startLat: Double,
            startLng: Double,
            endLat: Double,
            endLng: Double
        ): Float {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            return results[0]
        }

        fun calculateBearing(
            startLat: Double,
            startLng: Double,
            endLat: Double,
            endLng: Double
        ): Float {
            val results = FloatArray(2)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            var bearing = results[1]
            if (bearing < 0) {
                bearing += 360f
            }
            return bearing
        }

        fun formatDistance(meters: Float): String {
            return when {
                meters < 1000 -> "${meters.roundToInt()} m"
                else -> String.format("%.1f km", meters / 1000f)
            }
        }

        fun formatWalkingTime(meters: Float): String {
            // Average human walking pace: 1.35 m/s (~80 meters/min)
            val walkingMinutes = (meters / 80f).roundToInt()
            return when {
                walkingMinutes <= 1 -> "< 1 min a pé"
                walkingMinutes < 60 -> "~$walkingMinutes min a pé"
                else -> {
                    val hours = walkingMinutes / 60
                    val mins = walkingMinutes % 60
                    "~$hours h $mins min a pé"
                }
            }
        }
    }
}
