package com.example.pawcketdoc.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawcketdoc.dto.GrowthProgress
import com.example.pawcketdoc.model.Growth

@Dao
interface GrowthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(growth: Growth)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(growths: List<Growth>)

    @Query("SELECT * FROM Growth WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Growth>>

    @Query("DELETE FROM Growth WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT strftime('%m', g.dateRecorded) AS month, AVG(g.weight) AS averageWeight
        FROM Growth g 
        WHERE g.petId = :petId AND strftime('%Y', g.dateRecorded) = :year
        GROUP BY month
        ORDER BY month
        """)
    fun findWeightProgressByYear(petId: String, year: String): LiveData<List<GrowthProgress>>
}