package com.example.pawcketdoc.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pawcketdoc.dto.AppointmentMonthCount
import com.example.pawcketdoc.model.Appointment
import java.time.LocalDateTime

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<Appointment>)

    @Update
    suspend fun update(appointment: Appointment)

    @Query("DELETE FROM Appointment WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM Appointment WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Appointment>>

    @Query("""SELECT a.* FROM Pet p
            JOIN User u ON p.userId = u.id
            JOIN Appointment a ON p.id = a.petId
            WHERE u.id = :userId""")
    suspend fun findAllByUserId(userId: String): List<Appointment>

    @Query("""SELECT a.* FROM Pet p
            JOIN User u ON p.userId = u.id
            JOIN Appointment a ON p.id = a.petId
            WHERE u.id = :userId AND a.datetime >= :now""")
    fun findUpcomingByUserId(userId: String, now: LocalDateTime): LiveData<List<Appointment>>

    @Query("""
        SELECT strftime('%m', a.datetime) AS month, COUNT(*) AS appointmentCount
        FROM Appointment a
        INNER JOIN Pet p ON a.petId = p.id
        WHERE strftime('%Y', a.datetime) = :year
        AND p.userId = :userId
        GROUP BY month
        ORDER BY month
    """)
    suspend fun getAppointmentCountsPerMonth(userId: String, year: String): List<AppointmentMonthCount>
}