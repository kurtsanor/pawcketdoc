package com.example.tracker.database

import androidx.room.*

@Dao
interface PetDao {

    @Insert
    suspend fun insertPet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE userId = :userId")
    suspend fun getPetsByUserId(userId: Int): List<PetEntity>
}
