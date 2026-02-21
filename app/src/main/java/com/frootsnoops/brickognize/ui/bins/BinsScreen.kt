package com.frootsnoops.brickognize.ui.bins

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.Bitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.frootsnoops.brickognize.R
import com.frootsnoops.brickognize.domain.model.BrickItem
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BinsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (jsonString != null) {
                    viewModel.importBinLocations(jsonString)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to read import file")
            }
        }
    }
    
    // Handle export messages
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            viewModel.clearExportMessage()
        }
    }
    
    // Show import message
    uiState.importMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearImportMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bin Locations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Bins") },
                            onClick = {
                                showMenu = false
                                viewModel.exportBinLocations { jsonString ->
                                    shareExportFile(context, jsonString)
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Bins") },
                            onClick = {
                                showMenu = false
                                importLauncher.launch("application/json")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = {
            uiState.importMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(message)
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.bins.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No bins created yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create bins when assigning parts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                if (uiState.selectedBin != null) {
                    // Show parts in selected bin
                    BinDetailsView(
                        bin = uiState.selectedBin!!,
                        parts = uiState.partsInSelectedBin,
                        onBack = { viewModel.clearSelection() },
                        onDeletePart = { viewModel.deletePart(it) }
                    )
                } else {
                    // Show bins list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.bins) { binWithCount ->
                            BinCard(
                                bin = binWithCount.binLocation,
                                partCount = binWithCount.partCount,
                                previewParts = binWithCount.previewParts,
                                onClick = { viewModel.selectBin(binWithCount.binLocation) },
                                onDelete = { viewModel.deleteBin(binWithCount.binLocation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to share the exported JSON file
 */
private fun shareExportFile(context: Context, jsonString: String) {
    try {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = dateFormat.format(Date())
        val filename = "brickognize_bins_$timestamp.json"
        
        // Save to cache directory
        val file = File(context.cacheDir, filename)
        file.writeText(jsonString)
        
        // Create a content URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Brickognize Bin Locations Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Export Bin Locations"))
        Timber.i("Export file shared: $filename")
    } catch (e: Exception) {
        Timber.e(e, "Failed to share export file")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BinCard(
    bin: com.frootsnoops.brickognize.domain.model.BinLocation,
    partCount: Int,
    previewParts: List<BrickItem> = emptyList(),
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Bin?") },
            text = { 
                Text("Are you sure you want to delete \"${bin.label.uppercase(Locale.getDefault())}\"? All parts in this bin will also be deleted. This cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    showDeleteConfirm = true
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (previewParts.isNotEmpty()) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = bin.label.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    bin.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = "$partCount ${if (partCount == 1) "part" else "parts"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Preview images of parts (limited to 5)
            if (previewParts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    previewParts.forEach { part ->
                        Card(
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            val context = LocalContext.current
                            val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 50.dp.roundToPx() }
                            val request = ImageRequest.Builder(context)
                                .data(part.imgUrl)
                                .size(sizePx, sizePx)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .bitmapConfig(Bitmap.Config.RGB_565) // non-critical thumbnail
                                .build()
                            AsyncImage(
                                model = request,
                                contentDescription = part.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
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
fun BinDetailsView(
    bin: com.frootsnoops.brickognize.domain.model.BinLocation,
    parts: List<BrickItem>,
    onBack: () -> Unit,
    onDeletePart: (BrickItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bin.label.uppercase(Locale.getDefault())) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bin.description?.let { desc ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            if (parts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No parts assigned to this bin yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(parts) { part ->
                    PartCard(part = part, onDelete = { onDeletePart(part) })
                }
            }
        }
    }
}

@Composable
fun PartCard(part: BrickItem, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Part?") },
            text = { Text("Are you sure you want to delete \"${part.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showDeleteConfirm = true
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            Card(
                modifier = Modifier.size(60.dp)
            ) {
                val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 60.dp.roundToPx() }
                val request = ImageRequest.Builder(context)
                    .data(part.imgUrl)
                    .size(sizePx, sizePx)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .bitmapConfig(Bitmap.Config.RGB_565) // non-critical card thumbnail
                    .build()
                AsyncImage(
                    model = request,
                    contentDescription = part.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "ID: ${part.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                part.category?.let { category ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // BrickLink icon button to view part on BrickLink
            IconButton(
                onClick = {
                    val url = "https://www.bricklink.com/v2/catalog/catalogitem.page?P=${part.id}#T=C"
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
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
    }
}
