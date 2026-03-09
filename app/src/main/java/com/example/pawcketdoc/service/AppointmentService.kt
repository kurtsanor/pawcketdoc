package com.example.pawcketdoc.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.pawcketdoc.dao.AppointmentDao
import com.example.pawcketdoc.dto.AppointmentMonthCount
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.util.AppointmentUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class AppointmentService(
    private val appointmentDao: AppointmentDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<Appointment>> {
        return appointmentDao.findAllByPetId(petId)
    }

    suspend fun insert(appointment: Appointment) {
        val appointmentsRef = firebaseFirestore
            .collection("appointments")
            .document()
        val id = appointmentsRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        appointmentsRef.set(mapOf(
            "userId" to userId,
            "petId" to appointment.petId,
            "title" to appointment.title,
            "notes" to appointment.notes,
            "location" to appointment.location,
            "datetime" to appointment.datetime.toString(),
            "status" to appointment.status
        )).await()

        appointmentDao.insert(appointment.copy(id = id))
    }

    suspend fun deleteById(id: String) {
        firebaseFirestore.collection("appointments")
            .document(id)
            .delete()
            .await()

        appointmentDao.deleteById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findUpcomingByUserId(userId: String): LiveData<List<Appointment>> {
        val now = LocalDateTime.now()
        return appointmentDao.findUpcomingByUserId(userId, now)
    }

    suspend fun getAppointmentCountsPerMonth(userId: String, year: String): List<AppointmentMonthCount> {
        val rawAppointments = appointmentDao.getAppointmentCountsPerMonth(userId, year)
        return AppointmentUtil.fillMissingMonths(rawAppointments)
    }


}