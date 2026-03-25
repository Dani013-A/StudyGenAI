package com.studygenai.domain.usecase.auth

import com.studygenai.domain.model.User
import com.studygenai.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Email and password are required."))
        return repository.signIn(email, password)
    }
}