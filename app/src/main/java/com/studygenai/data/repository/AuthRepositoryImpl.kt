package com.studygenai.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studygenai.domain.model.User
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid   = firebaseUser.uid,
            email = firebaseUser.email ?: ""
        )
    }

    override fun authStateFlow(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.let {
                User(uid = it.uid, email = it.email ?: "")
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null after sign up")

            // Save user profile to Firestore
            val userDoc = mapOf(
                "uid"         to uid,
                "fullName"    to fullName,
                "email"       to email,
                "isOnboarded" to false,
                "createdAt"   to System.currentTimeMillis()
            )
            firestore
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(userDoc)
                .await()

            Result.success(User(uid = uid, fullName = fullName, email = email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null after sign in")
            val email2 = result.user?.email ?: ""
            Result.success(User(uid = uid, email = email2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setOnboarded(uid: String): Result<Unit> {
        return try {
            firestore
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update("isOnboarded", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isOnboarded(uid: String): Boolean {
        return try {
            val doc = firestore
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()
            doc.getBoolean("isOnboarded") ?: false
        } catch (e: Exception) {
            false
        }
    }
}