package com.example.pawcketdoc.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawcketdoc.dto.PetMedicationCount
import com.example.pawcketdoc.model.Medication
import java.time.LocalDate

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Query("SELECT * FROM Medication WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Medication>>

    @Query("DELETE FROM Medication WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""SELECT p.name AS petName, COUNT(m.id) AS medicationCount
        FROM Pet p LEFT JOIN Medication m ON p.id = m.petId
        AND :today BETWEEN m.startDate AND m.endDate 
        WHERE p.userId = :userId GROUP BY p.id
    """)
    suspend fun findActiveMedicationCountsByUserId(userId: String, today: LocalDate): List<PetMedicationCount>
}