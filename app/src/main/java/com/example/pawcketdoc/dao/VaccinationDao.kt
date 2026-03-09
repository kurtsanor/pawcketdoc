package com.example.pawcketdoc.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawcketdoc.model.Vaccination

@Dao
interface VaccinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: Vaccination)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vaccinations: List<Vaccination>)

    @Query("SELECT * FROM Vaccination WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Vaccination>>

    @Query("DELETE FROM Vaccination WHERE id = :id")
    suspend fun deleteById(id: String)
}