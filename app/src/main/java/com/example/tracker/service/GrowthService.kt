package com.example.tracker.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.tracker.dao.GrowthDao
import com.example.tracker.dto.GrowthProgress
import com.example.tracker.model.Growth
import com.example.tracker.util.GrowthUtil

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

    fun findWeightProgressByYear(petId: Long, year: String): LiveData<List<GrowthProgress>>  {
        return growthDao.findWeightProgressByYear(petId, year).map { list ->
            GrowthUtil.fillMissingMonths(list)
        }
    }
}