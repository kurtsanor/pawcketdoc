package com.example.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val petId: Long,
    val title: String,
    val notes: String,
    val date: LocalDate
)
