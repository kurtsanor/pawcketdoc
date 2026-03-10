package com.example.pawcketdoc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Document(
    @PrimaryKey
    val id: String = "",
    val petId: String = "",
    val name: String = "",
    val type: String = "",
    val notes: String = "",
    val fileUrl: String = "",
    val publicId: String = "",
    val dateIssued: LocalDate? = null,
)