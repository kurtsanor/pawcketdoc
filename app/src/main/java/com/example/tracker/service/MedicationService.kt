package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.tracker.dao.MedicationDao
import com.example.tracker.dto.HealthAnalytics
import com.example.tracker.model.Medication
import com.example.tracker.util.HealthUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class MedicationService(
    private val medicationDao: MedicationDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<Medication>> {
        return medicationDao.findAllByPetId(petId)
    }

    suspend fun insert(medication: Medication) {
        val medicationRef = firebaseFirestore
            .collection("medications")
            .document()
        val id = medicationRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        medicationRef.set(mapOf(
            "userId" to userId,
            "petId" to medication.petId,
            "name" to medication.name,
            "dosage" to medication.dosage,
            "frequency" to medication.frequency,
            "startDate" to medication.startDate.toString(),
            "endDate" to medication.endDate.toString(),
            "reason" to medication.reason,
            "notes" to medication.notes
        )).await()

        medicationDao.insert(medication.copy(id = id))
    }

    suspend fun deleteById(id: String) {
        firebaseFirestore.collection("medications")
            .document(id)
            .delete()
            .await()

        medicationDao.deleteById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPetsHealthAnalytics(userId: String): HealthAnalytics {
        val today = LocalDate.now()
        val medicationCounts = medicationDao.findActiveMedicationCountsByUserId(userId, today)

        return HealthUtil.calculateHealthAnalytics(medicationCounts)
    }
}