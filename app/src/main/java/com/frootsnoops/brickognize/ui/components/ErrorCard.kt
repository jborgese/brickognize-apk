package com.frootsnoops.brickognize.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frootsnoops.brickognize.domain.model.ErrorIcon
import com.frootsnoops.brickognize.domain.model.UserError

/**
 * Reusable error display component with icon, message, and action button.
 */
@Composable
fun ErrorCard(
    error: UserError,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error icon
            Icon(
                imageVector = error.icon.toImageVector(),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            // Error title
            Text(
                text = error.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            // Error message
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                // Dismiss button (if provided)
                if (onDismiss != null) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Dismiss")
                    }
                }
                
                // Action button (if applicable)
                val actionText = error.actionText
                if (onAction != null && actionText != null) {
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(actionText)
                    }
                }
            }
        }
    }
}

/**
 * Inline error message (smaller, less prominent).
 */
@Composable
fun InlineErrorMessage(
    error: UserError,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = error.icon.toImageVector(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = error.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error snackbar with action.
 */
@Composable
fun ErrorSnackbar(
    error: UserError,
    snackbarHostState: SnackbarHostState,
    onAction: (() -> Unit)? = null
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                actionColor = MaterialTheme.colorScheme.error
            )
        }
    )
}

/**
 * Convert ErrorIcon enum to Material Icon.
 */
fun ErrorIcon.toImageVector(): ImageVector = when (this) {
    ErrorIcon.NO_WIFI -> Icons.Default.WifiOff
    ErrorIcon.ERROR -> Icons.Default.Error
    ErrorIcon.SEARCH -> Icons.Default.SearchOff
    ErrorIcon.IMAGE -> Icons.Default.BrokenImage
    ErrorIcon.STORAGE -> Icons.Default.SdCardAlert
}
