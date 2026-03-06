package com.example.tracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id: String = "",
    val firstName: String,
    val surName: String,
)
