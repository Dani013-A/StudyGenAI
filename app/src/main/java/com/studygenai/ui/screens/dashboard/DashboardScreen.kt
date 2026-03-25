package com.studygenai.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studygenai.domain.model.Note
import com.studygenai.navigation.Screen
import com.studygenai.ui.theme.*

// ─── Data models for UI ───────────────────────────────────────────────────────

data class FeatureItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val iconColor: Color,
    val route: String
)

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val features = listOf(
        FeatureItem(
            label     = "Upload Notes",
            icon      = Icons.Outlined.Upload,
            tint      = BlueCardTint,
            iconColor = RoyalBlue,
            route     = Screen.Upload.route
        ),
        FeatureItem(
            label     = "AI Summarize",
            icon      = Icons.Outlined.AutoAwesome,
            tint      = PurpleCardTint,
            iconColor = LavenderPurple,
            route     = Screen.Library.route
        ),
        FeatureItem(
            label     = "Scan Image",
            icon      = Icons.Outlined.PhotoCamera,
            tint      = GreenCardTint,
            iconColor = SoftGreen,
            route     = Screen.Upload.route
        ),
        FeatureItem(
            label     = "Quiz Maker",
            icon      = Icons.Outlined.Quiz,
            tint      = PinkCardTint,
            iconColor = BlushPink,
            route     = Screen.QuizSetup.route
        )
    )

    val bottomNavItems = listOf(
        BottomNavItem("Home",     Icons.Filled.Home,     Icons.Outlined.Home,     Screen.Dashboard.route),
        BottomNavItem("Files",    Icons.Filled.Folder,   Icons.Outlined.Folder,   Screen.Library.route),
        BottomNavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, Screen.Settings.route)
    )

    Scaffold(
        containerColor = NeutralGray,
        bottomBar = {
            DashboardBottomBar(
                items           = bottomNavItems,
                selectedIndex   = selectedNavIndex,
                onItemSelected  = { index ->
                    selectedNavIndex = index
                    navController.navigate(bottomNavItems[index].route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { navController.navigate(Screen.Upload.route) },
                containerColor     = DarkNavy,
                contentColor       = SurfaceWhite,
                shape              = CircleShape,
                modifier           = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Greeting ──────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(28.dp))
                DashboardGreeting(userName = uiState.userName)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Search bar ────────────────────────────────────────────────────
            item {
                DashboardSearchBar(
                    query    = searchQuery,
                    onChange = { searchQuery = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Feature grid ──────────────────────────────────────────────────
            item {
                DashboardFeatureGrid(
                    features      = features,
                    onFeatureClick = { navController.navigate(it) }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // ── Recent header ─────────────────────────────────────────────────
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Recent",
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = DarkNavy
                    )
                    TextButton(onClick = { navController.navigate(Screen.Library.route) }) {
                        Text(
                            text     = "View all",
                            fontSize = 13.sp,
                            color    = RoyalBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Recent notes list ─────────────────────────────────────────────
            if (uiState.recentNotes.isEmpty()) {
                item {
                    Box(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Text(
                            text      = "No notes yet. Upload your first note!",
                            color     = TextSecondary,
                            fontSize  = 13.sp
                        )
                    }
                }
            } else {
                items(uiState.recentNotes) { note ->
                    RecentNoteRow(
                        note    = note,
                        onClick = {
                            navController.navigate(Screen.Summary.createRoute(note.id))
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ─── Greeting ─────────────────────────────────────────────────────────────────

@Composable
fun DashboardGreeting(userName: String) {
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else      -> "Good evening"
        }
    }
    Column {
        Text(
            text       = "$greeting, ${userName.ifBlank { "Student" }} 👋",
            fontSize   = 13.sp,
            color      = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = "Ready to study?",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = DarkNavy
        )
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
fun DashboardSearchBar(query: String, onChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector       = Icons.Outlined.Search,
                contentDescription = null,
                tint              = TextSecondary,
                modifier          = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicSearchField(query = query, onChange = onChange)
        }
    }
}

@Composable
fun BasicSearchField(query: String, onChange: (String) -> Unit) {
    if (query.isEmpty()) {
        Text(
            text     = "Search...",
            color    = TextSecondary,
            fontSize = 14.sp
        )
    }
    androidx.compose.foundation.text.BasicTextField(
        value         = query,
        onValueChange = onChange,
        textStyle     = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            color    = DarkNavy
        ),
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true
    )
}

// ─── Feature grid ─────────────────────────────────────────────────────────────

@Composable
fun DashboardFeatureGrid(
    features: List<FeatureItem>,
    onFeatureClick: (String) -> Unit
) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                features.take(2).forEach { feature ->
                    FeatureCard(
                        feature  = feature,
                        onClick  = { onFeatureClick(feature.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                features.drop(2).forEach { feature ->
                    FeatureCard(
                        feature  = feature,
                        onClick  = { onFeatureClick(feature.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), ambientColor = feature.iconColor, spotColor = feature.iconColor)
            .clip(RoundedCornerShape(20.dp))
            .background(feature.tint)
            .clickable { onClick() }
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier         = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = feature.icon,
                    contentDescription = feature.label,
                    tint               = feature.iconColor,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text       = feature.label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkNavy
            )
        }
    }
}

// ─── Recent note row ──────────────────────────────────────────────────────────

@Composable
fun RecentNoteRow(note: Note, onClick: () -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier         = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Description,
                    contentDescription = null,
                    tint               = RoyalBlue,
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = note.title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = DarkNavy,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = formatRelativeTime(note.createdAt),
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }

            // Three dots menu
            Icon(
                imageVector        = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint               = TextSecondary,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Bottom nav ───────────────────────────────────────────────────────────────

@Composable
fun DashboardBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .shadow(elevation = 8.dp)
    ) {
        NavigationBar(
            containerColor = SurfaceWhite,
            tonalElevation = 0.dp
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick  = { onItemSelected(index) },
                    icon     = {
                        Icon(
                            imageVector = if (selectedIndex == index)
                                item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    },
                    label    = {
                        Text(
                            text       = item.label,
                            fontSize   = 11.sp,
                            fontWeight = if (selectedIndex == index)
                                FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = NavSelected,
                        unselectedIconColor = NavUnselected,
                        selectedTextColor   = NavSelected,
                        unselectedTextColor = NavUnselected,
                        indicatorColor      = NeutralGray
                    )
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now  = System.currentTimeMillis()
    val diff = now - timestamp
    val mins = diff / 60_000
    val hrs  = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        mins < 1   -> "Just now"
        mins < 60  -> "$mins minutes ago"
        hrs  < 2   -> "Today, ${formatHourMinute(timestamp)}"
        hrs  < 24  -> "Today, ${formatHourMinute(timestamp)}"
        days == 1L -> "Yesterday"
        else       -> "$days days ago"
    }
}

fun formatHourMinute(timestamp: Long): String {
    val cal  = java.util.Calendar.getInstance()
    cal.timeInMillis = timestamp
    val hr   = cal.get(java.util.Calendar.HOUR)
    val min  = cal.get(java.util.Calendar.MINUTE)
    val ampm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
    return "$hr:${min.toString().padStart(2, '0')} $ampm"
}