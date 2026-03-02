package com.example.tracker.service


import androidx.sqlite.throwSQLiteException
import com.example.tracker.dao.CredentialsDao
import com.example.tracker.dao.UserDao
import com.example.tracker.dto.LoginRequest
import com.example.tracker.dto.SignUpRequest
import com.example.tracker.model.Credentials
import com.example.tracker.model.User
import org.mindrot.jbcrypt.BCrypt
import java.lang.RuntimeException

class AuthService(
    private val userDao: UserDao,
    private val credentialsDao: CredentialsDao
) {

    suspend fun register(signUpRequest: SignUpRequest) {
        val emailExists = credentialsDao.existsEmail(signUpRequest.email) != null
        if (emailExists) {
            throw RuntimeException("Email already exists")
        }
        val user = User(
            firstName = signUpRequest.firstName,
            surName = signUpRequest.surName
        )
        val generatedId = userDao.insert(user)

        val credentials = Credentials(
            userId = generatedId,
            email = signUpRequest.email,
            password = BCrypt.hashpw(signUpRequest.password, BCrypt.gensalt())
        )
        credentialsDao.insert(credentials)
    }

    suspend fun login(loginRequest: LoginRequest): Long? {
        val credentials: Credentials? = credentialsDao.findByEmail(loginRequest.email)
        if (credentials == null) {
            throw RuntimeException("Invalid email or password")
        }
        val isValidCredentials = BCrypt.checkpw(loginRequest.password, credentials.password)
        if (!isValidCredentials) {
            throw RuntimeException("Invalid email or password")
        }
        // return user id if login is successful
        return credentials.userId
    }

    suspend fun changeUserPassword(credentials: Credentials) {
        credentialsDao.update(credentials)
    }

    suspend fun findCredentialsByUserId(userId: Long): Credentials {
        return credentialsDao.findByUserId(userId)
    }
}