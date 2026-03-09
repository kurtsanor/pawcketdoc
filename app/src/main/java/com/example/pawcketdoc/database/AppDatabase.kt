package com.example.pawcketdoc.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pawcketdoc.dao.AppointmentDao
import com.example.pawcketdoc.dao.CredentialsDao
import com.example.pawcketdoc.dao.GrowthDao
import com.example.pawcketdoc.dao.MedicalRecordDao
import com.example.pawcketdoc.dao.MedicationDao
import com.example.pawcketdoc.dao.PetDao
import com.example.pawcketdoc.dao.UserDao
import com.example.pawcketdoc.dao.VaccinationDao
import com.example.pawcketdoc.model.Appointment
import com.example.pawcketdoc.model.Credentials
import com.example.pawcketdoc.model.Growth
import com.example.pawcketdoc.model.MedicalRecord
import com.example.pawcketdoc.model.Medication
import com.example.pawcketdoc.model.Pet
import com.example.pawcketdoc.model.User
import com.example.pawcketdoc.model.Vaccination
import com.example.pawcketdoc.util.Converters

@Database(entities = [
    Appointment::class,
    Growth::class,
    MedicalRecord::class,
    Medication::class,
    Pet::class,
    User::class,
    Vaccination::class,
    Credentials::class,
], version = 14)
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