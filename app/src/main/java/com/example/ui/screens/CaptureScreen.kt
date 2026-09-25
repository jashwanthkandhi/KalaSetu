package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.core.model.AppLanguage
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomActionBar
import com.example.ui.components.LanguageSelector
import com.example.ui.components.VoiceRecorderButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.dashedBorder
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel
import java.io.File

@Composable fun CaptureScreen(
    viewModel: KalaSetuViewModel,
    onNavigateToProcessing: () -> Unit,
    onNavigateToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val image by viewModel.capturedImageUri.collectAsState()
    val audio by viewModel.recordedAudioPath.collectAsState()
    val recording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDurationSeconds.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val online by viewModel.isOnline.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var showTips by rememberSaveable { mutableStateOf(false) }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.setCapturedPhoto(it.toString()) }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.setCapturedPhoto(it) }
    }
    val cameraUnavailableMessage = translated("Camera unavailable. Choose a photo instead.", language)
    val microphonePermissionMessage = translated("Microphone permission is needed to record your description.", language)

    fun launchCamera() {
        val dir = File(context.filesDir, "images").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri.toString()
        runCatching { camera.launch(uri) }.onFailure {
            Toast.makeText(context, cameraUnavailableMessage, Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startRecording() else viewModel.message.value = microphonePermissionMessage
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.testTag("capture_screen")
    ) {
        // App Header with Online/Offline indicator
        item {
            AppHeader(
                title = label("Add Product"),
                trailing = {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (online) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, if (online) Color(0xFFA5D6A7) else Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (online) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Text(
                                text = label(if (online) "Online" else "Offline"),
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (online) Color(0xFF15803D) else Color(0xFFB45309),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            )
        }

        // Language Selector
        item {
            LanguageSelector(language, viewModel::setLanguage, isCompact = true)
        }

        // STEP 1: CAPTURE PHOTO
        item {
            CaptureStep("1", "Capture your product", image != null, Icons.Outlined.CameraAlt)
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .dashedBorder(1.5.dp, KalaPrimary.copy(alpha = 0.4f), 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (image != null) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.35f).clip(RoundedCornerShape(14.dp))) {
                            AsyncImage(
                                model = image,
                                contentDescription = label("Product photo"),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF7ED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = KalaPrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = label("Tap to capture product photo"),
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label("Choose a clear photo of your product."),
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // Photo Action Buttons
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { cameraPermission.launch(Manifest.permission.CAMERA) },
                    enabled = !busy,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label("Take photo"), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { gallery.launch("image/*") },
                    enabled = !busy,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label("Choose photo"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Photo Tips Chip Button
        item {
            Surface(
                onClick = { showTips = true },
                shape = RoundedCornerShape(100.dp),
                color = KalaBanner,
                border = BorderStroke(1.dp, Color(0xFFFED7AA))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFFC2410C), modifier = Modifier.size(18.dp))
                    Text(
                        text = label("Tips for a beautiful listing"),
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                    )
                }
            }
        }

        // STEP 2: VOICE STORY
        item {
            CaptureStep("2", "Tell your product story", audio != null && !recording, Icons.Outlined.Mic)
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = label("Describe the material, size and how you made it. Record up to 29 seconds."),
                        style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        VoiceRecorderButton(
                            isRecording = recording,
                            onClick = {
                                if (recording) viewModel.stopRecording()
                                else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }

                    WaveformVisualizer(isRecording = recording)

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (recording) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                            }
                            Text(
                                text = "${duration / 60}:${(duration % 60).toString().padStart(2, '0')}",
                                style = Typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (recording) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            )
                        }
                        Text(
                            text = label(if (recording) "Recording your product story" else "Speak about your product in your language"),
                            style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (audio != null && !recording) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::playRecordedAudio,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label("Play recording"), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { micPermission.launch(Manifest.permission.RECORD_AUDIO) },
                        enabled = !busy,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label("Re-record"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!online) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Text(
                        text = label("Your photo and recording will wait safely for a connection. You will review the listing before it is published."),
                        modifier = Modifier.padding(14.dp),
                        style = Typography.bodySmall.copy(color = Color(0xFF92400E))
                    )
                }
            }
        }

        // Bottom Action Bar
        item {
            BottomActionBar(
                buttonText = label("Generate listing"),
                onButtonClick = { viewModel.triggerGenerateListing(onNavigateToProcessing, onNavigateToCatalog) },
                enabled = image != null && audio != null && !recording && !busy,
                footnote = label("Both a photo and a voice recording are required.")
            )
        }
    }

    if (showTips) {
        AlertDialog(
            onDismissRequest = { showTips = false },
            title = {
                Text(label("Tips for a beautiful listing"), style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📷", fontSize = 20.sp)
                        Text(label("Photo tip: use daylight and a simple background to show the texture of your craft."))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🎙️", fontSize = 20.sp)
                        Text(label("Describe the material, size and how you made it. Record up to 29 seconds."))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🔒", fontSize = 20.sp)
                        Text(label("Your photo and voice note are saved on this device."))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTips = false }) {
                    Text(label("Got it"), fontWeight = FontWeight.Bold, color = KalaPrimary)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
