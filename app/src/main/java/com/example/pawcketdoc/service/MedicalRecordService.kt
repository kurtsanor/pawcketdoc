package com.example.pawcketdoc.service

import androidx.lifecycle.LiveData
import com.example.pawcketdoc.dao.MedicalRecordDao
import com.example.pawcketdoc.model.MedicalRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MedicalRecordService(
    private val medicalRecordDao: MedicalRecordDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<MedicalRecord>> {
        return medicalRecordDao.findAllByPetId(petId)
    }

    suspend fun deleteById(id: String) {
        firebaseFirestore.collection("medicalRecords")
            .document(id)
            .delete()
            .await()

        medicalRecordDao.deleteById(id)
    }

    suspend fun insert(medicalRecord: MedicalRecord) {
        val medicalRef = firebaseFirestore
            .collection("medicalRecords")
            .document()
        val id = medicalRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        medicalRef.set(mapOf(
            "userId" to userId,
            "petId" to medicalRecord.petId,
            "title" to medicalRecord.title,
            "date" to medicalRecord.date.toString(),
            "diagnosis" to medicalRecord.diagnosis,
            "treatment" to medicalRecord.treatment,
            "notes" to medicalRecord.notes
        ))

        medicalRecordDao.insert(medicalRecord.copy(id = id))
    }
}