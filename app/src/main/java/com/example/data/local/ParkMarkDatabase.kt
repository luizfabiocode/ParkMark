package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ParkingSpot

@Database(entities = [ParkingSpot::class], version = 1, exportSchema = false)
abstract class ParkMarkDatabase : RoomDatabase() {
    abstract fun parkingSpotDao(): ParkingSpotDao

    companion object {
        @Volatile
        private var INSTANCE: ParkMarkDatabase? = null

        fun getInstance(context: Context): ParkMarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkMarkDatabase::class.java,
                    "parkmark_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
