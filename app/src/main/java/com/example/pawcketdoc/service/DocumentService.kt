package com.example.pawcketdoc.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.pawcketdoc.dao.DocumentDao
import com.example.pawcketdoc.model.Document
import com.example.pawcketdoc.model.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.RuntimeException
import java.time.LocalDate

class DocumentService(
    private val documentDao: DocumentDao,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun findAllByPetId(petId: String): LiveData<List<Document>>  {
        return documentDao.findAllByPetId(petId)
    }

    suspend fun insert(document: Document) {
        val documentRef = firebaseFirestore.collection("documents").document()
        val id = documentRef.id

        val userId = firebaseAuth.currentUser?.uid!!
        documentRef.set(mapOf(
            "userId" to userId,
            "petId" to document.petId,
            "name" to document.name,
            "type" to document.type,
            "notes" to document.notes,
            "fileUrl" to document.fileUrl,
            "publicId" to document.publicId,
            "dateIssued" to document.dateIssued.toString()
        )).await()

        documentDao.insert(document.copy(id = id))
    }

    suspend fun deleteById(id: String) {
        if (id.isBlank()) {
            throw RuntimeException("Invalid id")
        }
        firebaseFirestore.collection("documents")
            .document(id)
            .delete()
            .await()

        documentDao.deleteById(id)
    }


}