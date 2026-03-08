package com.example.tracker.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM User")
    suspend fun findAll(): List<User>

    @Query("SELECT * FROM User WHERE id = :id")
    fun findByIdLiveData(id: String): LiveData<User>

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun findById(id: Long): User
}