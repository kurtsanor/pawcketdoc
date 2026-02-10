package com.example.tracker.model

import java.time.LocalDate

data class MedicalRecord(
    val id: Long?,
    val petId: Long,
    val title: String,
    val date: LocalDate,
    val diagnosis: String,
    val treatment: String,
    val notes: String
)
