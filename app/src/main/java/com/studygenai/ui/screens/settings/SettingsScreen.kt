package com.studygenai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studygenai.navigation.Screen
import com.studygenai.ui.screens.dashboard.BottomNavItem
import com.studygenai.ui.screens.dashboard.DashboardBottomBar
import com.studygenai.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.studygenai.R

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to login on sign out
    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem("Home",     Icons.Filled.Home,     Icons.Outlined.Home,     Screen.Dashboard.route),
        BottomNavItem("Files",    Icons.Filled.Folder,   Icons.Outlined.Folder,   Screen.Library.route),
        BottomNavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, Screen.Settings.route)
    )

    Scaffold(
        containerColor = NeutralGray,
        bottomBar = {
            DashboardBottomBar(
                items          = bottomNavItems,
                selectedIndex  = 2,
                onItemSelected = { index ->
                    navController.navigate(bottomNavItems[index].route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text       = "Settings",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkNavy
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Profile card ──────────────────────────────────────────────────
            Card(
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circle with initials
                    Box(
                        modifier         = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = uiState.user.fullName
                                .split(" ")
                                .take(2)
                                .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                                .ifBlank { "S" },
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = SurfaceWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = uiState.user.fullName.ifBlank { "Student" },
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DarkNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text     = uiState.user.email,
                            fontSize = 13.sp,
                            color    = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Preferences section ───────────────────────────────────────────
            SettingsSectionLabel("Preferences")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsToggleRow(
                        icon       = Icons.Outlined.DarkMode,
                        iconColor  = LavenderPurple,
                        iconBg     = PurpleCardTint,
                        label      = "Dark Mode",
                        sublabel   = "Switch app appearance",
                        checked    = uiState.isDarkMode,
                        onToggle   = { viewModel.toggleDarkMode(it) },
                        showDivider = true
                    )
                    SettingsToggleRow(
                        icon       = Icons.Outlined.Notifications,
                        iconColor  = SoftGreen,
                        iconBg     = GreenCardTint,
                        label      = "Notifications",
                        sublabel   = "Study reminders and alerts",
                        checked    = uiState.notificationsEnabled,
                        onToggle   = { viewModel.toggleNotifications(it) },
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── About section ─────────────────────────────────────────────────
            SettingsSectionLabel("About")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsInfoRow(
                        icon      = Icons.Outlined.Info,
                        iconColor = RoyalBlue,
                        iconBg    = BlueCardTint,
                        label     = "Version",
                        value     = "1.0.0",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        icon      = Icons.Outlined.School,
                        iconColor = BlushPink,
                        iconBg    = PinkCardTint,
                        label     = "Made for",
                        value     = "TIP Students",
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Sign out ──────────────────────────────────────────────────────
            var showSignOutDialog by remember { mutableStateOf(false) }

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Logout,
                            contentDescription = null,
                            tint               = Color(0xFFFF3B30),
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    TextButton(
                        onClick  = { showSignOutDialog = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text       = stringResource(R.string.action_sign_out),
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFFFF3B30)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showSignOutDialog) {
                SignOutConfirmDialog(
                    onConfirm = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    },
                    onDismiss = { showSignOutDialog = false }
                )
            }

            if (uiState.isSigningOut) {
                com.studygenai.ui.components.LoadingOverlay(message = "Signing out...")
            }
        }
    }
}

// ─── Reusable rows ────────────────────────────────────────────────────────────

@Composable
fun SettingsSectionLabel(label: String) {
    Text(
        text          = label.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier      = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    sublabel: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconColor,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = DarkNavy
                )
                Text(
                    text     = sublabel,
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }
            Switch(
                checked         = checked,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = SurfaceWhite,
                    checkedTrackColor   = RoyalBlue,
                    uncheckedThumbColor = SurfaceWhite,
                    uncheckedTrackColor = NavUnselected
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color    = DividerColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun SettingsInfoRow(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconColor,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text       = label,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = DarkNavy,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text     = value,
                fontSize = 13.sp,
                color    = TextSecondary
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color    = DividerColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ─── Sign out dialog ──────────────────────────────────────────────────────────

@Composable
fun SignOutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        shape            = RoundedCornerShape(20.dp),
        icon             = {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = null,
                    tint               = Color(0xFFFF3B30),
                    modifier           = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = stringResource(R.string.action_sign_out),
                fontWeight = FontWeight.Bold,
                fontSize   = 17.sp,
                color      = DarkNavy
            )
        },
        text = {
            Text(
                text       = "Are you sure you want to sign out of your account?",
                fontSize   = 14.sp,
                color      = TextSecondary,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                shape   = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.action_sign_out), color = SurfaceWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}