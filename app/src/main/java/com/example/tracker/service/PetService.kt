package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.PetDao
import com.example.tracker.model.Pet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.RuntimeException
import java.time.LocalDate

class PetService(
    private val petDao: PetDao,
    private val firebaseFirestore: FirebaseFirestore
) {
    fun findAllByUserId(userId: String): LiveData<List<Pet>>  {
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
        val petDocumentRef = firebaseFirestore.collection("pets").document()
        val id = petDocumentRef.id

        petDocumentRef.set(mapOf(
            "userId" to pet.userId,
            "name" to pet.name,
            "type" to pet.type,
            "breed" to pet.breed,
            "gender" to pet.gender,
            "birthDate" to pet.birthDate.toString()
        )).await()

        petDao.insert(pet.copy(id = id))
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

    suspend fun deleteById(id: String) {
        if (id.isBlank()) {
            throw RuntimeException("Invalid id")
        }
        firebaseFirestore.collection("pets")
            .document(id)
            .delete()
            .await()

        petDao.deleteById(id)
    }

    suspend fun findById(id: String): Pet {
        return petDao.findById(id)
    }

}