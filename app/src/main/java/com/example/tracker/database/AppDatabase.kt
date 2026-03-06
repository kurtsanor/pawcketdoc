package com.example.tracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tracker.dao.AppointmentDao
import com.example.tracker.dao.CredentialsDao
import com.example.tracker.dao.GrowthDao
import com.example.tracker.dao.MedicalRecordDao
import com.example.tracker.dao.MedicationDao
import com.example.tracker.dao.PetDao
import com.example.tracker.dao.UserDao
import com.example.tracker.dao.VaccinationDao
import com.example.tracker.model.Appointment
import com.example.tracker.model.Credentials
import com.example.tracker.model.Growth
import com.example.tracker.model.MedicalRecord
import com.example.tracker.model.Medication
import com.example.tracker.model.Pet
import com.example.tracker.model.User
import com.example.tracker.model.Vaccination
import com.example.tracker.util.Converters

@Database(entities = [
    Appointment::class,
    Growth::class,
    MedicalRecord::class,
    Medication::class,
    Pet::class,
    User::class,
    Vaccination::class,
    Credentials::class,
], version = 10)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun growthDao(): GrowthDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun medicationDao(): MedicationDao
    abstract fun petDao(): PetDao
    abstract fun userDao(): UserDao
    abstract fun vaccinationDao(): VaccinationDao

    abstract fun credentialsDao(): CredentialsDao
}