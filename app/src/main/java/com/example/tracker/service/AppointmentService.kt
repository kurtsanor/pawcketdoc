package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.AppointmentDao
import com.example.tracker.dto.AppointmentMonthCount
import com.example.tracker.model.Appointment
import com.example.tracker.util.AppointmentUtil
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

    suspend fun getAppointmentCountsPerMonth(userId: Long, year: String): List<AppointmentMonthCount> {
        val rawAppointments = appointmentDao.getAppointmentCountsPerMonth(userId, year)
        return AppointmentUtil.fillMissingMonths(rawAppointments)
    }


}