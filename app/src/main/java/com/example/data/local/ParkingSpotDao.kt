package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ParkingSpot
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSpotDao {
    @Query("SELECT * FROM parking_spots WHERE id = 1 LIMIT 1")
    fun getSavedSpot(): Flow<ParkingSpot?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSpot(spot: ParkingSpot)

    @Query("UPDATE parking_spots SET notes = :notes WHERE id = 1")
    suspend fun updateNotes(notes: String)

    @Query("UPDATE parking_spots SET levelOrRow = :levelOrRow WHERE id = 1")
    suspend fun updateLevelOrRow(levelOrRow: String)

    @Query("DELETE FROM parking_spots WHERE id = 1")
    suspend fun clearSpot()
}
