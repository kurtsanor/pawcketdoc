package com.example.pawcketdoc.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawcketdoc.model.Document
import com.example.pawcketdoc.model.Growth
import com.example.pawcketdoc.model.Pet

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: Document)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<Document>)

    @Query("DELETE FROM Document WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM Document WHERE petId = :petId")
    fun findAllByPetId(petId: String): LiveData<List<Document>>


}