package com.example.tracker.model

import java.time.LocalDate

data class Vaccination(
    val id: Long?,
    val petId: Long,
    val name: String,
    val notes: String,
    val administeredDate: LocalDate
)
