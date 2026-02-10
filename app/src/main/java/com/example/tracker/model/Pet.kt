package com.example.tracker.model

import java.time.LocalDate

data class Pet(
    val id: Long?,
    val userId: Long,
    val name: String,
    val type: String,
    val breed: String,
    val gender: String,
    val birthDate: LocalDate
)
