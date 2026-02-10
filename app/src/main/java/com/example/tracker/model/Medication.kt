package com.example.tracker.model

import java.time.LocalDate

data class Medication(
    val id: Long?,
    val petId: Long,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String,
    val notes: String,
)
