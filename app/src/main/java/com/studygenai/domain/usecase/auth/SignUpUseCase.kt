package com.studygenai.domain.usecase.auth

import com.studygenai.domain.model.User
import com.studygenai.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String
    ): Result<User> {
        // Basic validation
        if (fullName.isBlank()) return Result.failure(Exception("Full name is required."))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.failure(Exception("Please enter a valid email."))
        if (password.length < 8)
            return Result.failure(Exception("Password must be at least 8 characters."))
        return repository.signUp(fullName, email, password)
    }
}