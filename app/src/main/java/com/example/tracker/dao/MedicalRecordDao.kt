package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.model.MedicalRecord

@Dao
interface MedicalRecordDao {
    @Insert
    suspend fun insert(medicalRecord: MedicalRecord)

    @Query("SELECT * FROM MedicalRecord WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<MedicalRecord>>

    @Query("DELETE FROM MedicalRecord WHERE id = :id")
    suspend fun deleteById(id: Long)
}