package com.example.pawcketdoc.model

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
data class Growth(
    @PrimaryKey
    val id: String = "",
    val petId: String,
    val weight: Float,
    val height: Float,
    val notes: String,
    val dateRecorded: LocalDate
)
