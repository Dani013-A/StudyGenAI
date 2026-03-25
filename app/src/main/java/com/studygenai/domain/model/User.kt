package com.studygenai.domain.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val isOnboarded: Boolean = false
)