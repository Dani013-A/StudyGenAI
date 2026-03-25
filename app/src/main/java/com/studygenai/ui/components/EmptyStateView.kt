package com.studygenai.ui.components

import androidx.compose.runtime.Composable

// TODO: Shown when a list is empty
// Shows illustration + message + optional CTA button
// Usage: EmptyStateView(message = "No notes yet", ctaLabel = "Upload your first note", onCta = { ... })
@Composable
fun EmptyStateView(message: String, ctaLabel: String? = null, onCta: (() -> Unit)? = null) {
    // Implementation goes here
}
