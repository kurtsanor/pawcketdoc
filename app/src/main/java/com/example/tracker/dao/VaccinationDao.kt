package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.model.Vaccination

@Dao
interface VaccinationDao {
    @Insert
    suspend fun insert(vaccination: Vaccination)

    @Query("SELECT * FROM Vaccination WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Vaccination>>

    @Query("DELETE FROM Vaccination WHERE id = :id")
    suspend fun deleteById(id: String)
}