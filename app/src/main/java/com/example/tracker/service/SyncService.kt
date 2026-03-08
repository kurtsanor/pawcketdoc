package com.example.tracker.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.tracker.dao.AppointmentDao
import com.example.tracker.dao.GrowthDao
import com.example.tracker.dao.MedicalRecordDao
import com.example.tracker.dao.MedicationDao
import com.example.tracker.dao.PetDao
import com.example.tracker.dao.UserDao
import com.example.tracker.dao.VaccinationDao
import com.example.tracker.model.Appointment
import com.example.tracker.model.Growth
import com.example.tracker.model.MedicalRecord
import com.example.tracker.model.Medication
import com.example.tracker.model.Pet
import com.example.tracker.model.User
import com.example.tracker.model.Vaccination
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime


class SyncService(
    private val firebaseFirestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val petDao: PetDao,
    private val appointmentDao: AppointmentDao,
    private val medicationDao: MedicationDao,
    private val growthDao: GrowthDao,
    private val vaccinationDao: VaccinationDao,
    private val medicalRecordDao: MedicalRecordDao
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncAll(userId: String) {
        syncUser(userId)
        syncPets(userId)
        coroutineScope {
            launch { syncAppointments(userId) }
            launch { syncMedications(userId) }
            launch { syncGrowth(userId) }
            launch { syncVaccinations(userId) }
            launch { syncMedicalRecords(userId) }
        }
    }

    private suspend fun syncUser(userId: String) {
        val snapshot = firebaseFirestore
            .collection("users")
            .document(userId)
            .get()
            .await()

        val user = User(
            id = snapshot.id,
            firstName = snapshot.getString("firstName") ?: "",
            surName = snapshot.getString("surName") ?: ""
        )

        userDao.insert(user)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncPets(userId: String) {
        val snapshot = firebaseFirestore
            .collection("pets")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val pets = snapshot.documents.map { doc ->
            Pet(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                name = doc.getString("name") ?: "",
                type = doc.getString("type") ?: "",
                breed = doc.getString("breed") ?: "",
                gender = doc.getString("gender") ?: "",
                birthDate = LocalDate.parse(doc.getString("birthDate"))
            )
        }

        petDao.insertAll(pets)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncAppointments(userId: String) {
        val snapshot = firebaseFirestore
            .collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val appointments = snapshot.documents.map { doc ->
            Appointment(
                id = doc.id,
                petId = doc.getString("petId") ?: "",
                title = doc.getString("title") ?: "",
                notes = doc.getString("notes") ?: "",
                location = doc.getString("location") ?: "",
                datetime = LocalDateTime.parse(doc.getString("datetime")),
                status = doc.getString("status") ?: ""
            )
        }

        appointmentDao.insertAll(appointments)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncMedications(userId: String) {
        val snapshot = firebaseFirestore
            .collection("medications")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val medications = snapshot.documents.map { doc ->
            Medication(
                id = doc.id,
                petId = doc.getString("petId") ?: "",
                name = doc.getString("name") ?: "",
                dosage = doc.getString("dosage") ?: "",
                frequency = doc.getString("frequency") ?: "",
                startDate = LocalDate.parse(doc.getString("startDate")),
                endDate = LocalDate.parse(doc.getString("endDate")),
                reason = doc.getString("reason") ?: "",
                notes = doc.getString("notes") ?: ""
            )
        }

        medicationDao.insertAll(medications)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncGrowth(userId: String) {
        val snapshot = firebaseFirestore
            .collection("growths")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val growths = snapshot.documents.map { doc ->
            Growth(
                id = doc.id,
                petId = doc.getString("petId") ?: "",
                weight = (doc.getDouble("weight") ?: 0.0).toFloat(),
                height = (doc.getDouble("height") ?: 0.0).toFloat(),
                notes = doc.getString("notes") ?: "",
                dateRecorded = LocalDate.parse(doc.getString("dateRecorded"))
            )
        }

        growthDao.insertAll(growths)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncVaccinations(userId: String) {
        val snapshot = firebaseFirestore
            .collection("vaccinations")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val vaccinations = snapshot.documents.map { doc ->
            Vaccination(
                id = doc.id,
                petId = doc.getString("petId") ?: "",
                name = doc.getString("name") ?: "",
                notes = doc.getString("notes") ?: "",
                administeredDate = LocalDate.parse(doc.getString("administeredDate"))
            )
        }

        vaccinationDao.insertAll(vaccinations)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncMedicalRecords(userId: String) {
        val snapshot = firebaseFirestore
            .collection("medicalRecords")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val medicalRecords = snapshot.documents.map { doc ->
            MedicalRecord(
                id = doc.id,
                petId = doc.getString("petId") ?: "",
                title = doc.getString("title") ?: "",
                date = LocalDate.parse(doc.getString("date")),
                diagnosis = doc.getString("diagnosis") ?: "",
                treatment = doc.getString("treatment") ?: "",
                notes = doc.getString("notes") ?: ""
            )
        }

        medicalRecordDao.insertAll(medicalRecords)
    }
}