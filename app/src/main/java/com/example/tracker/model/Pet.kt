package com.example.tracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = ["id"],
    childColumns = ["userId"],
    onDelete = ForeignKey.CASCADE
)],
    indices = [Index(value = ["userId"])])
data class Pet(
    @PrimaryKey
    val id: String = "",
    val userId: String,
    val name: String,
    val type: String,
    val breed: String,
    val gender: String,
    val birthDate: LocalDate
)
