package com.studygenai.ui.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studygenai.domain.model.Note
import com.studygenai.navigation.Screen
import com.studygenai.ui.screens.dashboard.BottomNavItem
import com.studygenai.ui.screens.dashboard.DashboardBottomBar
import com.studygenai.ui.screens.dashboard.formatRelativeTime
import com.studygenai.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.studygenai.R

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

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
                selectedIndex  = 1,
                onItemSelected = { index ->
                    navController.navigate(bottomNavItems[index].route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.Upload.route) },
                containerColor = DarkNavy,
                contentColor   = SurfaceWhite,
                shape          = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Header
            Text(
                text       = "My Files",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkNavy
            )
            Text(
                text     = "${uiState.allNotes.size} notes",
                fontSize = 13.sp,
                color    = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            LibrarySearchBar(
                query    = uiState.searchQuery,
                onChange = { viewModel.onSearchQueryChanged(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes list
            if (uiState.filteredNotes.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint               = NavUnselected,
                            modifier           = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text      = if (uiState.searchQuery.isBlank())
                                "No notes yet" else "No results for \"${uiState.searchQuery}\"",
                            color     = TextSecondary,
                            fontSize  = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (uiState.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text     = "Tap + to upload your first note",
                                color    = NavUnselected,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = uiState.filteredNotes,
                        key   = { it.id }
                    ) { note ->
                        SwipeToDeleteNoteCard(
                            note     = note,
                            onClick  = { selectedNote = note },
                            onDelete = { noteToDelete = note }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }

    // Note detail dialog
    selectedNote?.let { note ->
        NoteDetailDialog(
            note    = note,
            onDismiss = { selectedNote = null },
            onDelete  = {
                noteToDelete = note
                selectedNote = null
            }
        )
    }

    // Delete confirmation dialog
    noteToDelete?.let { note ->
        DeleteConfirmDialog(
            noteTitle = note.title,
            onConfirm = {
                viewModel.deleteNote(note.id)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }

    // Error snackbar
    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearError()
        }
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
fun LibrarySearchBar(query: String, onChange: (String) -> Unit) {
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
                imageVector        = Icons.Outlined.Search,
                contentDescription = null,
                tint               = TextSecondary,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                if (query.isEmpty()) {
                    Text(stringResource(R.string.hint_search_notes), color = TextSecondary, fontSize = 14.sp)
                }
                BasicTextField(
                    value         = query,
                    onValueChange = onChange,
                    textStyle     = TextStyle(fontSize = 14.sp, color = DarkNavy),
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
        }
    }
}

// ─── Swipe to delete card ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state             = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    EndToStart -> Color(0xFFFF3B30)
                    else       -> Color(0xFFFFEEEE)
                },
                label = "swipe_bg"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == EndToStart) 1f else 0.8f,
                label       = "icon_scale"
            )
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint               = SurfaceWhite,
                    modifier           = Modifier.scale(scale)
                )
            }
        },
        content = {
            NoteCard(note = note, onClick = onClick)
        }
    )
}

// ─── Note card ────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
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
            Box(
                modifier         = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Description,
                    contentDescription = null,
                    tint               = RoyalBlue,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

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
                if (note.subject.isNotBlank()) {
                    Text(
                        text     = note.subject,
                        fontSize = 11.sp,
                        color    = RoyalBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text     = formatRelativeTime(note.createdAt),
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
            }

            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = NavUnselected,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Note detail dialog ───────────────────────────────────────────────────────

@Composable
fun NoteDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = note.title,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DarkNavy,
                            maxLines   = 2,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (note.subject.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text       = note.subject,
                                fontSize   = 12.sp,
                                color      = RoyalBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint               = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = DividerColor)

                // Raw OCR text content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text       = "Extracted Text",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val scrollState = rememberScrollState()
                    Text(
                        text      = note.rawText.ifBlank {
                            "No text was extracted from this note."
                        },
                        fontSize  = 14.sp,
                        color     = if (note.rawText.isBlank()) TextSecondary
                        else DarkNavy,
                        lineHeight = 22.sp,
                        modifier  = Modifier.verticalScroll(scrollState)
                    )
                }

                HorizontalDivider(color = DividerColor)

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF3B30)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, Color(0xFFFF3B30)
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_delete), fontSize = 14.sp)
                    }

                    Button(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text(stringResource(R.string.action_done), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─── Delete confirmation dialog ───────────────────────────────────────────────

@Composable
fun DeleteConfirmDialog(
    noteTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = SurfaceWhite,
        shape             = RoundedCornerShape(20.dp),
        icon              = {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint               = Color(0xFFFF3B30),
                    modifier           = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text       = "Delete Note",
                fontWeight = FontWeight.Bold,
                fontSize   = 17.sp,
                color      = DarkNavy
            )
        },
        text = {
            Text(
                text     = "\"$noteTitle\" will be permanently deleted. This cannot be undone.",
                fontSize = 14.sp,
                color    = TextSecondary,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                shape   = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.action_delete), color = SurfaceWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}
