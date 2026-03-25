package com.studygenai

import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.activity.enableEdgeToEdge
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.navigation.NavGraph
import com.studygenai.ui.theme.StudyGenAITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences("studygenai_prefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }

            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                    if (key == "dark_mode") {
                        isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            StudyGenAITheme(darkTheme = isDarkMode) {
                NavGraph(authRepository = authRepository)
            }
        }
    }
}