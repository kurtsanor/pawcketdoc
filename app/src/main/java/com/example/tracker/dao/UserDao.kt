package com.example.tracker.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tracker.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM User")
    suspend fun findAll(): List<User>

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun findById(id: Long): User
}