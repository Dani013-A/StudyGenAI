package com.studygenai.ui.screens.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studygenai.navigation.Screen
import com.studygenai.ui.components.LoadingOverlay
import com.studygenai.ui.theme.DarkNavy
import com.studygenai.ui.theme.NeutralGray
import com.studygenai.ui.theme.RoyalBlue
import com.studygenai.ui.theme.SurfaceWhite

@Composable
fun OtpScreen(
    navController: NavController,
    email: String,
    flow: String,
    name: String,
    password: String,
    pendingOtp: String,
    otpGeneratedAt: Long,
    viewModel: OtpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var otpValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.pendingOtp = pendingOtp
        viewModel.otpGeneratedAt = otpGeneratedAt
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is OtpUiState.NavigateToDashboard -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is OtpUiState.NavigateToOnboarding -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeutralGray,
                        RoyalBlue.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back button
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Check your email",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "We sent a 6-digit OTP to",
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = email,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = RoyalBlue
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Floating-style 6-box OTP input
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OtpInputField(
                        otp = otpValue,
                        onOtpChanged = { if (it.length <= 6) otpValue = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Verify Button
            Button(
                onClick = {
                    viewModel.verifyAndProceed(
                        enteredOtp = otpValue,
                        flow = flow,
                        email = email,
                        fullName = name,
                        password = password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                enabled = otpValue.length == 6 && uiState !is OtpUiState.Loading
            ) {
                if (uiState is OtpUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "Verify OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState is OtpUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as OtpUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive the code? ",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                TextButton(
                    onClick = {
                        viewModel.resendOtp(email, name.ifBlank { "Student" })
                    },
                    enabled = uiState !is OtpUiState.ResendingOtp && uiState !is OtpUiState.Loading
                ) {
                    Text(
                        text = "Resend",
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Loading overlays
        if (uiState is OtpUiState.Loading) {
            LoadingOverlay(message = "Verifying OTP...")
        }
        if (uiState is OtpUiState.ResendingOtp) {
            LoadingOverlay(message = "Resending OTP...")
        }
    }
}

@Composable
fun OtpInputField(otp: String, onOtpChanged: (String) -> Unit) {
    BasicTextField(
        value = otp,
        onValueChange = onOtpChanged,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = if (otp.length == index) RoyalBlue else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}