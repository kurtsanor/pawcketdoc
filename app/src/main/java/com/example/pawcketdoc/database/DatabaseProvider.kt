package com.example.pawcketdoc.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pawcketdoc_db"
            )
                .fallbackToDestructiveMigration(true)
                .build()
            INSTANCE = instance
            return instance
        }
    }
}