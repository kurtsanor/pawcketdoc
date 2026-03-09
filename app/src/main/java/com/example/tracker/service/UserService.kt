package com.example.tracker.service

import androidx.lifecycle.LiveData
import com.example.tracker.dao.UserDao
import com.example.tracker.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserService(
    private val userDao: UserDao,
    private val firebaseFirestore: FirebaseFirestore
) {
    suspend fun findById(id: String): User  {
        return userDao.findById(id)
    }

    fun findByIdLiveData(id: String): LiveData<User>  {
        return userDao.findByIdLiveData(id)
    }

    suspend fun update(user: User) {
        firebaseFirestore.collection("users")
            .document(user.id)
            .update(mapOf
                (
                    "uid" to user.id,
                    "firstName" to user.firstName,
                    "surName" to user.surName
                )
            )
            .await()

        userDao.update(user)
    }
}