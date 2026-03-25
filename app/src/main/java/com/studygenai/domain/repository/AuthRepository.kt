package com.studygenai.domain.repository

import com.studygenai.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Returns current logged-in user, or null
    fun getCurrentUser(): User?

    // Auth state as a flow — emits User? whenever auth state changes
    fun authStateFlow(): Flow<User?>

    // Creates Firebase Auth account + saves user doc to Firestore
    suspend fun signUp(fullName: String, email: String, password: String): Result<User>

    // Signs in with Firebase Auth
    suspend fun signIn(email: String, password: String): Result<User>

    // Signs out
    suspend fun signOut(): Result<Unit>

    // Marks the user as onboarded in Firestore
    suspend fun setOnboarded(uid: String): Result<Unit>

    // Checks if user has completed onboarding
    suspend fun isOnboarded(uid: String): Boolean
}