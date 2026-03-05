package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.dto.GrowthProgress
import com.example.tracker.model.Growth

@Dao
interface GrowthDao {
    @Insert
    suspend fun insert(growth: Growth)

    @Query("SELECT * FROM Growth WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<Growth>>

    @Query("DELETE FROM Growth WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT strftime('%m', g.dateRecorded) AS month, AVG(g.weight) AS averageWeight
        FROM Growth g 
        WHERE g.petId = :petId AND strftime('%Y', g.dateRecorded) = :year
        GROUP BY month
        ORDER BY month
        """)
    fun findWeightProgressByYear(petId: Long, year: String): LiveData<List<GrowthProgress>>
}