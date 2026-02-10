package com.example.tracker.database

import com.example.tracker.model.Vaccination

// Convert Room entity → UI model
fun VaccinationEntity.toModel(): Vaccination =
    Vaccination(
        id = id,
        petId = petId,
        name = title,                    // entity field → model field
        notes = notes,
        administeredDate = date
    )

// Convert UI model → Room entity (if needed)
fun Vaccination.toEntity(): VaccinationEntity =
    VaccinationEntity(
        id = id,
        petId = petId,
        title = name,
        notes = notes,
        date = administeredDate
    )
