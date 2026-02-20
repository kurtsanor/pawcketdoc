package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.PetDao
import com.example.tracker.model.Pet
import java.lang.RuntimeException
import java.time.LocalDate

class PetService(private val petDao: PetDao) {
    fun findAllByUserId(userId: Long): LiveData<List<Pet>>  {
        return petDao.findAllByUserId(userId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(pet: Pet) {
        if (pet.name.length > 30) {
            throw RuntimeException("Name cannot exceed 30 characters")
        }
        if (pet.birthDate.isAfter(LocalDate.now())) {
            throw RuntimeException("Birthday cannot be in the future")
        }
        petDao.insert(pet)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(pet: Pet) {
        if (pet.name.length > 30) {
            throw RuntimeException("Name cannot exceed 30 characters")
        }
        if (pet.birthDate.isAfter(LocalDate.now())) {
            throw RuntimeException("Birthday cannot be in the future")
        }
        petDao.update(pet)
    }

    suspend fun deleteById(id: Long) {
        if (id < 0) {
            throw RuntimeException("Invalid id")
        }
        petDao.deleteById(id)
    }

}