package com.example.pawcketdoc.service

import androidx.lifecycle.LiveData
import com.example.pawcketdoc.dao.UserDao
import com.example.pawcketdoc.model.User
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

    suspend fun updateAvatar(userId: String, avatarUrl: String, avatarPublicId: String) {
        firebaseFirestore.collection("users").document(userId)
            .update(
                mapOf(
                    "avatarUrl" to avatarUrl,
                    "avatarPublicId" to avatarPublicId
                )
            )

        userDao.updateAvatar(userId, avatarUrl, avatarPublicId)
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