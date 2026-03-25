package com.studygenai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.ui.screens.login.LoginScreen
import com.studygenai.ui.screens.login.SignUpScreen
import com.studygenai.ui.screens.onboarding.OnboardingScreen
import com.studygenai.ui.screens.otp.OtpScreen
import java.net.URLDecoder
import com.studygenai.ui.screens.dashboard.DashboardScreen
import com.studygenai.ui.screens.library.LibraryScreen
import com.studygenai.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(authRepository: AuthRepository) {
    val navController = rememberNavController()

    val startDestination = if (authRepository.getCurrentUser() != null)
        Screen.Dashboard.route
    else
        Screen.Login.route

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(
                navArgument("email")       { type = NavType.StringType },
                navArgument("flow")        { type = NavType.StringType },
                navArgument("name")        { type = NavType.StringType },
                navArgument("password")    { type = NavType.StringType },
                navArgument("otp")         { type = NavType.StringType },
                navArgument("generatedAt") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val email       = URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8")
            val flow        = backStackEntry.arguments?.getString("flow") ?: "login"
            val name        = URLDecoder.decode(
                backStackEntry.arguments?.getString("name") ?: "", "UTF-8")
            val password    = URLDecoder.decode(
                backStackEntry.arguments?.getString("password") ?: "", "UTF-8")
            val otp         = backStackEntry.arguments?.getString("otp") ?: ""
            val generatedAt = backStackEntry.arguments?.getLong("generatedAt") ?: 0L

            OtpScreen(
                navController  = navController,
                email          = email,
                flow           = flow,
                name           = name,
                password       = password,
                pendingOtp     = otp,
                otpGeneratedAt = generatedAt
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(Screen.Upload.route)     { }

        composable(Screen.Library.route) {
            LibraryScreen(navController)
        }

        composable(Screen.Summary.route)    { }
        composable(Screen.QuizSetup.route)  { }
        composable(Screen.QuizActive.route) { }
        composable(Screen.QuizResult.route) { }
        composable(Screen.Flashcard.route)  { }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}