package com.example.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * This project version uses format-only login, so we default to userId=0.
     * Later you can replace this with a real user id from authentication.
     */
    val userId: Long = 0,

    val name: String,
    val type: String,
    val breed: String,
    val gender: String,
    val birthDate: LocalDate
)
