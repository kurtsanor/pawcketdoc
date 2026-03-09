package com.example.pawcketdoc.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.pawcketdoc.dao.GrowthDao
import com.example.pawcketdoc.dto.GrowthProgress
import com.example.pawcketdoc.model.Growth
import com.example.pawcketdoc.util.GrowthUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GrowthService(
    private val growthDao: GrowthDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<Growth>> {
        return growthDao.findAllByPetId(petId)
    }

    suspend fun insert(growth: Growth) {
        val growthRef = firebaseFirestore.collection("growths").document()
        val id = growthRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        growthRef.set(mapOf(
            "userId" to userId,
            "petId" to growth.petId,
            "weight" to growth.weight,
            "height" to growth.height,
            "notes" to growth.notes,
            "dateRecorded" to growth.dateRecorded.toString()
        )).await()

        growthDao.insert(growth.copy(id = id))
    }

    suspend fun deleteById(id: String) {
        firebaseFirestore.collection("growths")
            .document(id)
            .delete()
            .await()

        return growthDao.deleteById(id)
    }

    fun findWeightProgressByYear(petId: String, year: String): LiveData<List<GrowthProgress>>  {
        return growthDao.findWeightProgressByYear(petId, year).map { list ->
            GrowthUtil.fillMissingMonths(list)
        }
    }
}