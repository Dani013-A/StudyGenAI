package com.studygenai.domain.model

data class Note(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val rawText: String = "",
    val imageUrl: String = "",
    val createdAt: Long = 0L
)
