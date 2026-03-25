package com.studygenai.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object SignUp     : Screen("signup")
    object Otp        : Screen(
        "otp/{email}/{flow}/{name}/{password}/{otp}/{generatedAt}"
    ) {
        fun createRoute(
            email: String,
            flow: String,
            name: String,
            password: String,
            otp: String,
            generatedAt: Long
        ): String {
            val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
            return "otp/${enc(email)}/$flow/${enc(name)}/${enc(password)}/${enc(otp)}/$generatedAt"
        }
    }
    object Onboarding : Screen("onboarding")
    object Dashboard  : Screen("dashboard")
    object Upload     : Screen("upload")
    object Library    : Screen("library")
    object Summary    : Screen("summary/{noteId}") {
        fun createRoute(noteId: String) = "summary/$noteId"
    }
    object QuizSetup  : Screen("quiz/setup")
    object QuizActive : Screen("quiz/active")
    object QuizResult : Screen("quiz/result")
    object Flashcard  : Screen("flashcard/{noteId}") {
        fun createRoute(noteId: String) = "flashcard/$noteId"
    }
    object Settings   : Screen("settings")
}