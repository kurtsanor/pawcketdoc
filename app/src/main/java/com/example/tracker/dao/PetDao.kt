package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.model.Pet

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pets: List<Pet>)

    @Update
    suspend fun update(pet: Pet)

    @Query("DELETE FROM Pet WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM Pet WHERE userId = :userId")
    fun findAllByUserId(userId: String): LiveData<List<Pet>>

    @Query("SELECT * FROM Pet WHERE id =:id")
    suspend fun findById(id: String): Pet
}