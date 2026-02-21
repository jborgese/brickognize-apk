package com.frootsnoops.brickognize.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Format a timestamp to a human-readable date/time string.
 */
fun Long.toFormattedDateTime(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * Format a timestamp to a relative time string (e.g., "2 hours ago").
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> toFormattedDateTime()
    }
}
