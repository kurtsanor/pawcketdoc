package com.example.pawcketdoc.service

import androidx.lifecycle.LiveData
import com.example.pawcketdoc.dao.VaccinationDao
import com.example.pawcketdoc.model.Vaccination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VaccinationService(
    private val vaccinationDao: VaccinationDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<Vaccination>> {
        return vaccinationDao.findAllByPetId(petId)
    }

    suspend fun insert(vaccination: Vaccination) {
        if (vaccination.name.isEmpty()) {
            throw RuntimeException("Vaccination name is missing")
        }
        val vaccinationRef = firebaseFirestore
            .collection("vaccinations")
            .document()
        val id = vaccinationRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        vaccinationRef.set(mapOf(
            "userId" to userId,
            "petId" to vaccination.petId,
            "name" to vaccination.name,
            "notes" to vaccination.notes,
            "administeredDate" to vaccination.administeredDate.toString()
        )).await()

        vaccinationDao.insert(vaccination.copy(id = id))
    }

    suspend fun deleteById(id: String) {
        firebaseFirestore.collection("vaccinations")
            .document(id)
            .delete()
            .await()

        vaccinationDao.deleteById(id)
    }
}