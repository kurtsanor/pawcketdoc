package com.example.tracker.service

import com.example.tracker.dao.UserDao
import com.example.tracker.model.User

class UserService(private val userDao: UserDao) {
    suspend fun findById(id: Long): User {
        return userDao.findById(id)
    }
}