package com.example.pawcketdoc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id: String = "",
    val firstName: String,
    val surName: String,
    val avatarUrl: String? = null,
    val avatarPublicId: String? = null
)
