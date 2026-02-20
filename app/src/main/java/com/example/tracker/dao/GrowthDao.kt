package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.model.Growth

@Dao
interface GrowthDao {
    @Insert
    suspend fun insert(growth: Growth)

    @Query("SELECT * FROM Growth WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<Growth>>

    @Query("DELETE FROM Growth WHERE id = :id")
    suspend fun deleteById(id: Long)
}