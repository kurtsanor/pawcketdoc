package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.UserDao
import com.example.tracker.model.User

class UserService(private val userDao: UserDao) {
    suspend fun findById(id: Long): User  {
        return userDao.findById(id)
    }

    fun findByIdLiveData(id: String): LiveData<User>  {
        return userDao.findByIdLiveData(id)
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }
}