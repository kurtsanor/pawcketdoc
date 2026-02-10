package com.example.tracker.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Update
    suspend fun updatePet(pet: PetEntity) // optional

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePetById(petId: Long) // optional

    @Query("SELECT * FROM pets WHERE userId = :userId ORDER BY id DESC")
    fun getPetsByUserId(userId: Long): LiveData<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    fun observePetById(petId: Long): LiveData<PetEntity?>
}

