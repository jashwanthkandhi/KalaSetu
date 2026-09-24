package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.AppLanguage
import com.example.ui.components.BottomActionBar
import com.example.ui.components.VoiceRecorderButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.dashedBorder
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun CaptureScreen(
    viewModel: KalaSetuViewModel,
    onNavigateToProcessing: () -> Unit,
    onNavigateToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    val recordedAudioPath by viewModel.recordedAudioPath.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsState()

    var showPhotoPickerMenu by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setCapturedPhoto(it.toString()) }
    }

    // Camera Preview Bitmap Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = saveBitmapToCache(context, it)
            viewModel.setCapturedPhoto(uri.toString())
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.startRecording()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KalaBackground)
                    .padding(top = 8.dp)
            ) {
                // Top row: Online status indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF388E3C) else Color(0xFFD4A017))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isOnline) KalaSetuStrings.onlineStatus(currentLanguage) else KalaSetuStrings.offlineStatus(currentLanguage),
                        style = Typography.labelSmall.copy(
                            color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFB58000),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.clickable { viewModel.toggleOnlineStatus() }
                    )
                }

                // Main Top Bar: Back arrow, "Add Product" centered, and Language Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KalaText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = KalaSetuStrings.addProduct(currentLanguage),
                        style = Typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = KalaText
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Language Selector Pill (e.g. తెలుగు)
                    Box {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFF6ECE3))
                                .clickable { languageMenuExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentLanguage.nativeName,
                                style = Typography.labelMedium.copy(
                                    color = KalaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.nativeName) },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomActionBar(
                buttonText = "${KalaSetuStrings.generateListing(currentLanguage)} →",
                onButtonClick = {
                    // Default to demo pot if photo or audio not taken yet
                    if (capturedImageUri == null) {
                        viewModel.setCapturedPhoto("android.resource://${context.packageName}/drawable/ic_terracotta_pot_detailed")
                    }
                    viewModel.triggerGenerateListing(
                        onNavigateToProcessing = onNavigateToProcessing,
                        onNavigateToCatalog = onNavigateToCatalog
                    )
                },
                footnote = KalaSetuStrings.generateListingHelper(currentLanguage),
                testTag = "generate_listing_button"
            )
        },
        containerColor = KalaBackground,
        modifier = Modifier.testTag("capture_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. PRODUCT PHOTO CAPTURE CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KalaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable { showPhotoPickerMenu = true }
                    .testTag("photo_zone")
            ) {
                if (capturedImageUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Product Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = KalaSurface.copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clickable { showPhotoPickerMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retake",
                                    tint = KalaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = KalaSetuStrings.edit(currentLanguage),
                                    style = Typography.labelSmall.copy(
                                        color = KalaPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .dashedBorder(
                                width = 1.5.dp,
                                color = Color(0xFFD4C8C0),
                                cornerRadius = 14.dp,
                                dashLength = 8.dp,
                                gapLength = 6.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = KalaPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = KalaSetuStrings.tapToCapture(currentLanguage),
                                style = Typography.bodyMedium.copy(
                                    color = Color(0xFF2C1810),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }

            // Dropdown / sheet menu for camera vs gallery
            DropdownMenu(
                expanded = showPhotoPickerMenu,
                onDismissRequest = { showPhotoPickerMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(KalaSetuStrings.takeCameraPhoto(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = KalaPrimary) },
                    onClick = {
                        showPhotoPickerMenu = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
                DropdownMenuItem(
                    text = { Text(KalaSetuStrings.chooseFromGallery(currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Collections, contentDescription = null, tint = KalaPrimary) },
                    onClick = {
                        showPhotoPickerMenu = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. CONCENTRIC GLOWING VOICE RECORDER
            VoiceRecorderButton(
                isRecording = isRecording,
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. SOUND WAVEFORM
            WaveformVisualizer(
                isRecording = isRecording,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer & Re-record Action
            val seconds = if (isRecording) recordingDuration else 12
            val formattedTime = String.format("0:%02d", seconds)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedTime,
                    style = Typography.titleLarge.copy(
                        color = KalaText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = KalaSetuStrings.rerecord(currentLanguage),
                    style = Typography.bodyMedium.copy(
                        color = Color(0xFF6D5E57),
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        viewModel.clearRecording()
                        viewModel.startRecording()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = KalaSetuStrings.speakAboutProduct(currentLanguage),
                style = Typography.bodyMedium.copy(
                    color = Color(0xFF2C1810),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val cachePath = File(context.cacheDir, "captured_photos")
    if (!cachePath.exists()) cachePath.mkdirs()
    val file = File(cachePath, "photo_${System.currentTimeMillis()}.jpg")
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
    fos.flush()
    fos.close()
    return Uri.fromFile(file)
}
