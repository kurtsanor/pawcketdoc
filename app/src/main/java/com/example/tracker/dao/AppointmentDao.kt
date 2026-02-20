package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.model.Appointment
import com.example.tracker.model.Growth

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insert(appointment: Appointment)

    @Update
    suspend fun update(appointment: Appointment)

    @Query("DELETE FROM Appointment WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM Appointment WHERE petId = :petId")
    fun findAllByPetId(petId: Long): LiveData<List<Appointment>>

    @Query("""SELECT a.* FROM Pet p
            JOIN User u ON p.userId = u.id
            JOIN Appointment a ON p.id = a.petId
            WHERE u.id = :userId""")
    suspend fun findAllByUserId(userId: Long): List<Appointment>

}