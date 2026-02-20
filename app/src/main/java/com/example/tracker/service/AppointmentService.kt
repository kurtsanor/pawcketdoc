package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.AppointmentDao
import com.example.tracker.model.Appointment

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
}