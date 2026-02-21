package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.MedicationDao
import com.example.tracker.dto.HealthAnalytics
import com.example.tracker.model.Medication
import com.example.tracker.util.HealthUtil
import java.time.LocalDate

class MedicationService(private val medicationDao: MedicationDao) {
    fun findAllByPetId(petId: Long): LiveData<List<Medication>> {
        return medicationDao.findAllByPetId(petId)
    }

    suspend fun insert(medication: Medication) {
        medicationDao.insert(medication)
    }

    suspend fun deleteById(id: Long) {
        medicationDao.deleteById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPetsHealthAnalytics(userId: Long): HealthAnalytics {
        val today = LocalDate.now()
        val medicationCounts = medicationDao.findActiveMedicationCountsByUserId(userId, today)

        return HealthUtil.calculateHealthAnalytics(medicationCounts)
    }
}