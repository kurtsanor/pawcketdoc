package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.model.Medication

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: Medication)

    @Query("SELECT * FROM Medication WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<Medication>>

    @Query("DELETE FROM Medication WHERE id = :id")
    suspend fun deleteById(id: Long)
}