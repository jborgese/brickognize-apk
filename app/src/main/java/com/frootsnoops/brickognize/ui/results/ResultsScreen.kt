package com.frootsnoops.brickognize.ui.results

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.Bitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.frootsnoops.brickognize.R
import com.frootsnoops.brickognize.domain.model.BrickItem

enum class BinSortOption {
    ALPHABETICAL, LAST_MODIFIED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToScan: () -> Unit,
    recognitionResult: com.frootsnoops.brickognize.domain.model.RecognitionResult? = null,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    // Set the recognition result when the screen is composed
    LaunchedEffect(recognitionResult) {
        recognitionResult?.let { result ->
            viewModel.setRecognitionResult(result)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Keep system back behavior aligned with the app bar back action.
    BackHandler(enabled = !uiState.showBinPicker) {
        onNavigateBack()
    }
    
    // Show success message when present
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg)
            viewModel.clearFeedbackMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recognition Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToScan) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan Again")
                    }
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        uiState.recognitionResult?.let { result ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top candidate
                result.topCandidate?.let { topItem ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Top Match",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                BrickItemCard(
                                    item = topItem,
                                    isTopResult = true,
                                    onAssignBin = { viewModel.showBinPicker(topItem.id) },
                                    onFeedback = { isCorrect -> viewModel.submitFeedbackForItem(topItem, isCorrect) },
                                    isCoolingDown = (uiState.feedbackCooldownUntil ?: 0L) > System.currentTimeMillis(),
                                    cooldownUntil = uiState.feedbackCooldownUntil
                                )
                            }
                        }
                    }
                }
                
                // Other candidates
                if (result.candidates.size > 1) {
                    item {
                        Text(
                            text = "Other Matches",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    items(result.candidates.drop(1)) { item ->
                        BrickItemCard(
                            item = item,
                            isTopResult = false,
                            onAssignBin = { viewModel.showBinPicker(item.id) },
                            onFeedback = { isCorrect -> viewModel.submitFeedbackForItem(item, isCorrect) },
                            isCoolingDown = (uiState.feedbackCooldownUntil ?: 0L) > System.currentTimeMillis(),
                            cooldownUntil = uiState.feedbackCooldownUntil
                        )
                    }
                }
            }
            
            // Bin picker dialog
            if (uiState.showBinPicker) {
                BinPickerDialog(
                    availableBins = uiState.availableBins,
                    binLastModifiedAt = uiState.binLastModifiedAt,
                    onDismiss = { viewModel.hideBinPicker() },
                    onSelectBin = { binId -> 
                        viewModel.assignBinToPart(binId)
                    },
                    onCreateNewBin = { label, description ->
                        viewModel.assignBinToPart(null, label, description)
                    }
                )
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No results to display")
            }
        }
    }
}

@Composable
fun BrickItemCard(
    item: BrickItem,
    isTopResult: Boolean,
    onAssignBin: () -> Unit,
    onFeedback: (Boolean) -> Unit,
    @Suppress("UNUSED_PARAMETER") isCoolingDown: Boolean,
    cooldownUntil: Long?
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            Card(
                modifier = Modifier.size(80.dp)
            ) {
                val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 80.dp.roundToPx() }
                val builder = ImageRequest.Builder(context)
                    .data(item.imgUrl)
                    .size(sizePx, sizePx)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                // Treat top result image as critical: keep default ARGB_8888
                if (!isTopResult) {
                    builder.bitmapConfig(Bitmap.Config.RGB_565)
                }
                val request = builder.build()
                AsyncImage(
                    model = request,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Part name and BrickLink icon row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // BrickLink icon button
                    IconButton(
                        onClick = {
                            val url = "https://www.bricklink.com/v2/catalog/catalogitem.page?P=${item.id}#T=C"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(32.dp * 1.1f)
                    ) {
                        Icon(
                            painter = painterResource(id = com.frootsnoops.brickognize.R.drawable.bricklink_icon),
                            contentDescription = "View on BrickLink",
                            tint = Color.Unspecified
                        )
                    }
                }
                
                // Bin assignment - moved below Part name and doubled size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = item.binLocation?.label?.uppercase() ?: "No bin",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (item.binLocation != null) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "ID: ${item.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                item.score?.let { score ->
                    Text(
                        text = "Confidence: ${(score * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Change/Assign Bin button - now full width to match feedback buttons
                OutlinedButton(
                    onClick = onAssignBin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (item.binLocation != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (item.binLocation != null) "Change Bin" else "Assign Bin",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Feedback buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp)
                ) {
                    var remainingMs by remember { mutableStateOf(0L) }
                    LaunchedEffect(cooldownUntil) {
                        if (cooldownUntil != null && cooldownUntil > System.currentTimeMillis()) {
                            while (cooldownUntil > System.currentTimeMillis()) {
                                remainingMs = cooldownUntil - System.currentTimeMillis()
                                kotlinx.coroutines.delay(200)
                            }
                        }
                        remainingMs = 0L
                    }
                    val disabled = remainingMs > 0
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onFeedback(true) },
                            enabled = !disabled,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ThumbUp,
                                contentDescription = "Correct",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = { onFeedback(false) },
                            enabled = !disabled,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ThumbDown,
                                contentDescription = "Incorrect",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (disabled) {
                            val secs = ((remainingMs + 999) / 1000).toInt()
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("${secs}s", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinPickerDialog(
    availableBins: List<com.frootsnoops.brickognize.domain.model.BinLocation>,
    binLastModifiedAt: Map<Long, Long>,
    onDismiss: () -> Unit,
    onSelectBin: (Long) -> Unit,
    onCreateNewBin: (String, String?) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newBinLabel by remember { mutableStateOf("") }
    var newBinDescription by remember { mutableStateOf("") }
    
    // Sort option states
    var selectedSort by remember { mutableStateOf(BinSortOption.LAST_MODIFIED) }
    
    // Check if bin name already exists (case-insensitive)
    val isDuplicateBin = remember(newBinLabel, availableBins) {
        newBinLabel.isNotBlank() && 
        availableBins.any { it.label.equals(newBinLabel, ignoreCase = true) }
    }
    
    // Sort bins based on selected option
    val sortedBins = remember(availableBins, binLastModifiedAt, selectedSort) {
        when (selectedSort) {
            BinSortOption.ALPHABETICAL -> availableBins.sortedBy { it.label.uppercase() }
            BinSortOption.LAST_MODIFIED -> availableBins.sortedByDescending { bin ->
                binLastModifiedAt[bin.id] ?: bin.createdAt
            }
        }
    }
    
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Bin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBinLabel,
                        onValueChange = { newBinLabel = it },
                        label = { Text("Bin Label") },
                        placeholder = { Text("e.g., A1, Drawer 3") },
                        singleLine = true,
                        isError = isDuplicateBin,
                        supportingText = if (isDuplicateBin) {
                            { Text("A bin with this name already exists") }
                        } else null
                    )
                    OutlinedTextField(
                        value = newBinDescription,
                        onValueChange = { newBinDescription = it },
                        label = { Text("Description (optional)") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBinLabel.isNotBlank() && !isDuplicateBin) {
                            onCreateNewBin(
                                newBinLabel,
                                newBinDescription.takeIf { it.isNotBlank() }
                            )
                            showCreateDialog = false
                            onDismiss()
                        }
                    },
                    enabled = newBinLabel.isNotBlank() && !isDuplicateBin
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Bin Location") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Bin")
                        }
                    }
                    
                    if (availableBins.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        // Sort options
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Sort by:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = selectedSort == BinSortOption.LAST_MODIFIED,
                                        onClick = { selectedSort = BinSortOption.LAST_MODIFIED },
                                        label = { Text("Last Modified") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = selectedSort == BinSortOption.ALPHABETICAL,
                                        onClick = { selectedSort = BinSortOption.ALPHABETICAL },
                                        label = { Text("Alphabetical") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        items(sortedBins) { bin ->
                            Card(
                                onClick = { 
                                    onSelectBin(bin.id)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = bin.label.uppercase(),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    bin.description?.let { desc ->
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
