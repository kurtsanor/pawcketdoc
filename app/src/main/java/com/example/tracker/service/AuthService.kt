package com.example.tracker.service


import androidx.sqlite.throwSQLiteException
import com.example.tracker.dao.CredentialsDao
import com.example.tracker.dao.UserDao
import com.example.tracker.dto.LoginRequest
import com.example.tracker.dto.SignUpRequest
import com.example.tracker.model.Credentials
import com.example.tracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt
import java.lang.RuntimeException

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
                "lastName" to signUpRequest.surName
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

    suspend fun changeUserPassword(credentials: Credentials) {
        credentialsDao.update(credentials)
    }

    suspend fun findCredentialsByUserId(userId: Long): Credentials {
        return credentialsDao.findByUserId(userId)
    }
}