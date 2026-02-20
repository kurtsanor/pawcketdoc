package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.GrowthDao
import com.example.tracker.model.Growth

class GrowthService(private val growthDao: GrowthDao) {
    fun findAllByPetId(petId: Long): LiveData<List<Growth>> {
        return growthDao.findAllByPetId(petId)
    }

    suspend fun insert(growth: Growth) {
        return growthDao.insert(growth)
    }

    suspend fun deleteById(id: Long) {
        return growthDao.deleteById(id)
    }
}