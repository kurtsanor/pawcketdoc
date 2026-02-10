package com.example.tracker.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VaccinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity)

    @Delete
    suspend fun deleteVaccination(vaccination: VaccinationEntity)

    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY date DESC")
    fun observeVaccinationsByPetId(petId: Long): LiveData<List<VaccinationEntity>>
}
