package com.frootsnoops.brickognize.ui.scan

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.frootsnoops.brickognize.ui.components.ErrorCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit,
    viewModel: ScanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recognitionType by viewModel.recognitionType.collectAsState()
    val autoLaunchCamera by viewModel.autoLaunchCamera.collectAsState()
    val context = LocalContext.current
    
    // State to hold the temporary camera photo URI
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // State to trigger camera launch after permission is granted
    var shouldLaunchCamera by remember { mutableStateOf(false) }
    
    // Camera launcher - must be declared before permission launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                viewModel.processImage(uri)
            }
        }
        tempPhotoUri = null
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shouldLaunchCamera = true
        }
    }
    
    // Launch camera when permission is granted
    LaunchedEffect(shouldLaunchCamera) {
        if (shouldLaunchCamera) {
            shouldLaunchCamera = false
            tempPhotoUri = createTempImageUri(context)
            tempPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processImage(it)
        }
    }
    
    // Navigate to results on success
    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Success) {
            onNavigateToResults()
        }
    }
    
    // Auto-launch camera when coming from Results screen
    LaunchedEffect(autoLaunchCamera) {
        if (autoLaunchCamera) {
            viewModel.setAutoLaunchCamera(false)
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan ${recognitionType.name}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ScanUiState.Idle, is ScanUiState.Capturing -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Take or select a photo of your LEGO ${recognitionType.apiPath}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Recognition Mode Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.frootsnoops.brickognize.domain.model.RecognitionType.entries.forEach { type ->
                                FilterChip(
                                    selected = recognitionType == type,
                                    onClick = { viewModel.setRecognitionType(type) },
                                    label = { Text(type.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo")
                        }
                        
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select from Gallery")
                        }
                    }
                }
                
                is ScanUiState.Processing -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Recognizing...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Sending image to Brickognize API",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                is ScanUiState.Error -> {
                    ErrorCard(
                        error = state.error,
                        onAction = { viewModel.resetState() },
                        onDismiss = { viewModel.resetState() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                is ScanUiState.Success -> {
                    // This state is handled by LaunchedEffect
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Creates a temporary URI for camera capture using FileProvider.
 */
private fun createTempImageUri(context: Context): Uri? {
    return try {
        val imageFile = File(
            context.cacheDir,
            "camera_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
