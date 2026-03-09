package com.example.pawcketdoc.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pawcketdoc.model.Credentials

@Dao
interface CredentialsDao {
    @Insert
    suspend fun insert(credentials: Credentials)

    @Update
    suspend fun update(credentials: Credentials)

    @Query("SELECT 1 FROM Credentials WHERE email = :email LIMIT 1")
    suspend fun existsEmail(email: String): Int?

    @Query("SELECT * FROM Credentials WHERE email = :email")
    suspend fun findByEmail(email: String): Credentials?

    @Query("SELECT * FROM Credentials WHERE userId = :userId")
    suspend fun findByUserId(userId: Long): Credentials
}