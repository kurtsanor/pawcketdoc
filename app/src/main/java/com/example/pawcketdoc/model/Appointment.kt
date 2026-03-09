package com.example.pawcketdoc.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(foreignKeys = [ForeignKey(
    entity = Pet::class,
    parentColumns = ["id"],
    childColumns = ["petId"],
    onDelete = ForeignKey.CASCADE
)],
    indices = [Index(value = ["petId"])])
data class Appointment(
    @PrimaryKey
    val id: String = "",
    val petId: String,
    val title: String,
    val notes: String,
    val location: String,
    val datetime: LocalDateTime,
    val status: String,
)
