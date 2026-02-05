package com.example.tracker.database

import com.example.tracker.model.Pet

fun PetEntity.toModel(): Pet = Pet(
    id = this.id,
    userId = this.userId,
    name = this.name,
    type = this.type,
    breed = this.breed,
    gender = this.gender,
    birthDate = this.birthDate
)

fun Pet.toEntity(): PetEntity = PetEntity(
    id = this.id ?: 0,
    userId = this.userId,
    name = this.name,
    type = this.type,
    breed = this.breed,
    gender = this.gender,
    birthDate = this.birthDate
)
