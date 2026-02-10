package com.example.tracker.model

import java.time.LocalDate

data class Growth(
    val id: Long?,
    val petId: Long,
    val weight: Float,
    val height: Float,
    val notes: String,
    val dateRecorded: LocalDate
)
