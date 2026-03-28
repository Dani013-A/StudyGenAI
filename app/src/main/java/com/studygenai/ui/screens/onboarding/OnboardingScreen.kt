package com.studygenai.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studygenai.navigation.Screen
import com.studygenai.ui.theme.DarkNavy
import com.studygenai.ui.theme.NeutralGray
import com.studygenai.ui.theme.RoyalBlue
import com.studygenai.ui.theme.SurfaceWhite

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage("📸", "Scan Your Notes",
        "Take a photo of your handwritten notes and let AI extract the text instantly."),
    OnboardingPage("✨", "AI-Powered Summaries",
        "Get concise summaries of any lesson in seconds — study smarter, not harder."),
    OnboardingPage("🧠", "Quizzes & Flashcards",
        "Test yourself with auto-generated quizzes and flashcards built from your own notes.")
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var currentPage by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is OnboardingUiState.Done) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Page content
        val page = pages[currentPage]

        Text(text = page.emoji, fontSize = 72.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = page.description,
            fontSize = 15.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Dot indicators
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .background(
                            color = if (index == currentPage) RoyalBlue
                            else RoyalBlue.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next / Get Started button
        Button(
            onClick = {
                if (currentPage < pages.lastIndex) {
                    currentPage++
                } else {
                    viewModel.completeOnboarding()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            enabled = uiState !is OnboardingUiState.Loading
        ) {
            Text(
                text = if (currentPage < pages.lastIndex) "Next" else "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Skip button (only on first two pages)
        if (currentPage < pages.lastIndex) {
            TextButton(onClick = { viewModel.completeOnboarding() }) {
                Text("Skip", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}