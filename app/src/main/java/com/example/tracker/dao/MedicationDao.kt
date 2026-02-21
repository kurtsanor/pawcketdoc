package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.dto.PetMedicationCount
import com.example.tracker.model.Medication
import java.time.LocalDate

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: Medication)

    @Query("SELECT * FROM Medication WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<Medication>>

    @Query("DELETE FROM Medication WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""SELECT p.name AS petName, COUNT(m.id) AS medicationCount
        FROM Pet p LEFT JOIN Medication m ON p.id = m.petId
        AND :today BETWEEN m.startDate AND m.endDate 
        WHERE p.userId = :userId GROUP BY p.id
    """)
    suspend fun findActiveMedicationCountsByUserId(userId: Long, today: LocalDate): List<PetMedicationCount>
}