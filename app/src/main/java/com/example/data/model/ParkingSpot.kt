package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spots")
data class ParkingSpot(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val levelOrRow: String = ""
)
