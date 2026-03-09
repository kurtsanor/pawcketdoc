package com.example.pawcketdoc.service


import com.example.pawcketdoc.dao.CredentialsDao
import com.example.pawcketdoc.dao.UserDao
import com.example.pawcketdoc.dto.LoginRequest
import com.example.pawcketdoc.dto.SignUpRequest
import com.example.pawcketdoc.model.Credentials
import com.example.pawcketdoc.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthService(
    private val userDao: UserDao,
    private val credentialsDao: CredentialsDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun register(signUpRequest: SignUpRequest) {
        val result = firebaseAuth.createUserWithEmailAndPassword(signUpRequest.email, signUpRequest.password).await()
        val uid = result.user?.uid!!

        firebaseFirestore.collection("users")
            .document(uid)
            .set(mapOf(
                "uid" to uid,
                "firstName" to signUpRequest.firstName,
                "surName" to signUpRequest.surName
            )).await()

        // insert to room as well
        val user = User(
            id = uid,
            firstName = signUpRequest.firstName,
            surName = signUpRequest.surName
        )
        userDao.insert(user)
    }

    suspend fun login(loginRequest: LoginRequest) {
        firebaseAuth.signInWithEmailAndPassword(loginRequest.email, loginRequest.password).await()
    }

    suspend fun changeUserPassword(newPassword: String) {
        val user = firebaseAuth.currentUser!!
        user.updatePassword(newPassword).await()
    }

    suspend fun findCredentialsByUserId(userId: Long): Credentials {
        return credentialsDao.findByUserId(userId)
    }
}