package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.model.Pet

@Dao
interface PetDao {
    @Insert
    suspend fun insert(pet: Pet)

    @Update
    suspend fun update(pet: Pet)

    @Query("DELETE FROM Pet WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM Pet WHERE userId = :userId")
    fun findAllByUserId(userId: Long): LiveData<List<Pet>>
}