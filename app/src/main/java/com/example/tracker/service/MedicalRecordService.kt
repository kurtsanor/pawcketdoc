package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.MedicalRecordDao
import com.example.tracker.model.MedicalRecord

class MedicalRecordService(private val medicalRecordDao: MedicalRecordDao) {
    fun findAllByPetId(petId: Long): LiveData<List<MedicalRecord>> {
        return medicalRecordDao.findAllByPetId(petId)
    }

    suspend fun deleteById(id: Long) {
        medicalRecordDao.deleteById(id)
    }

    suspend fun insert(medicalRecord: MedicalRecord) {
        medicalRecordDao.insert(medicalRecord)
    }
}