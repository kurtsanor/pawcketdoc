package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.AppointmentDao
import com.example.tracker.model.Appointment
import java.time.LocalDateTime

class AppointmentService(private val appointmentDao: AppointmentDao) {
    fun findAllByPetId(petId: Long): LiveData<List<Appointment>> {
        return appointmentDao.findAllByPetId(petId)
    }

    suspend fun insert(appointment: Appointment) {
        appointmentDao.insert(appointment)
    }

    suspend fun deleteById(id: Long) {
        appointmentDao.deleteById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findUpcomingByUserId(userId: Long): LiveData<List<Appointment>> {
        val now = LocalDateTime.now()
        return appointmentDao.findUpcomingByUserId(userId, now)
    }
}