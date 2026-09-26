package com.example.data.repository

import com.example.data.local.ParkingSpotDao
import com.example.data.model.ParkingSpot
import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
    fun getSavedSpot(): Flow<ParkingSpot?>
    suspend fun saveSpot(spot: ParkingSpot)
    suspend fun updateNotes(notes: String)
    suspend fun updateLevelOrRow(levelOrRow: String)
    suspend fun clearSpot()
}

class ParkingRepositoryImpl(
    private val parkingSpotDao: ParkingSpotDao
) : ParkingRepository {

    override fun getSavedSpot(): Flow<ParkingSpot?> = parkingSpotDao.getSavedSpot()

    override suspend fun saveSpot(spot: ParkingSpot) {
        parkingSpotDao.saveSpot(spot)
    }

    override suspend fun updateNotes(notes: String) {
        parkingSpotDao.updateNotes(notes)
    }

    override suspend fun updateLevelOrRow(levelOrRow: String) {
        parkingSpotDao.updateLevelOrRow(levelOrRow)
    }

    override suspend fun clearSpot() {
        parkingSpotDao.clearSpot()
    }
}
