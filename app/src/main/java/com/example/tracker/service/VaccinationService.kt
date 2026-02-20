package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.VaccinationDao
import com.example.tracker.model.Vaccination

class VaccinationService(private val vaccinationDao: VaccinationDao) {
    fun findAllByPetId(petId: Long): LiveData<List<Vaccination>> {
        return vaccinationDao.findAllByPetId(petId)
    }

    suspend fun insert(vaccination: Vaccination) {
        if (vaccination.name.isEmpty()) {
            throw RuntimeException("Vaccination name is missing")
        }
        vaccinationDao.insert(vaccination)
    }

    suspend fun deleteById(id: Long) {
        vaccinationDao.deleteById(id)
    }
}