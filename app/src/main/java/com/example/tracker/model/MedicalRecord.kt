package com.example.tracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(foreignKeys = [ForeignKey(
    entity = Pet::class,
    parentColumns = ["id"],
    childColumns = ["petId"],
    onDelete = ForeignKey.CASCADE
)],
    indices = [Index(value = ["petId"])])
data class MedicalRecord(
    @PrimaryKey
    val id: String = "",
    val petId: String,
    val title: String,
    val date: LocalDate,
    val diagnosis: String,
    val treatment: String,
    val notes: String
)
