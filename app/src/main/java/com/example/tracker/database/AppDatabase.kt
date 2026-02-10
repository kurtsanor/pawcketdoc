package com.example.tracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PetEntity::class,
        VaccinationEntity::class   //  Added vaccination table
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao

    //  New DAO for vaccinations
    abstract fun vaccinationDao(): VaccinationDao
}
