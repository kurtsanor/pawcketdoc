package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.MedicationDao
import com.example.tracker.model.Medication

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
}