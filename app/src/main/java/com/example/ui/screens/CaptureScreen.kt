package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.core.model.AppLanguage
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomActionBar
import com.example.ui.components.LanguageSelector
import com.example.ui.components.VoiceRecorderButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.dashedBorder
import com.example.ui.viewmodel.KalaSetuViewModel
import java.io.File

@Composable fun CaptureScreen(viewModel: KalaSetuViewModel, onNavigateToProcessing: () -> Unit, onNavigateToCatalog: () -> Unit) {
    val context = LocalContext.current
    val image by viewModel.capturedImageUri.collectAsState()
    val audio by viewModel.recordedAudioPath.collectAsState()
    val recording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDurationSeconds.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val online by viewModel.isOnline.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.setCapturedPhoto(it.toString()) } }
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
        runCatching { camera.launch(uri) }.onFailure { Toast.makeText(context, cameraUnavailableMessage, Toast.LENGTH_LONG).show() }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) launchCamera() }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startRecording() else viewModel.message.value = microphonePermissionMessage
    }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.testTag("capture_screen")) {
        item { AppHeader(label("Add Product"), trailing = { Text(label(if (online) "Online" else "Offline"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }) }
        item { LanguageSelector(language, viewModel::setLanguage, isCompact = true) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp).dashedBorder(1.dp, MaterialTheme.colorScheme.outlineVariant, 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (image != null) AsyncImage(image, "Captured product", modifier = Modifier.fillMaxWidth().aspectRatio(1.35f))
                    else {
                        Text("▣", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(12.dp)); Text(label("Tap to capture product photo"), style = MaterialTheme.typography.titleLarge)
                        Text(label("Choose a clear photo of your product."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ cameraPermission.launch(Manifest.permission.CAMERA) }, enabled = !busy) { Text(label("Take photo")) }
            OutlinedButton({ gallery.launch("image/*") }, enabled = !busy) { Text(label("Choose photo")) }
        } }
        item {
            Text(label("Describe the material, size and how you made it. Record up to 29 seconds."), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                VoiceRecorderButton(recording, { if (recording) viewModel.stopRecording() else micPermission.launch(Manifest.permission.RECORD_AUDIO) })
            }
            WaveformVisualizer(recording)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${duration / 60}:${(duration % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.headlineMedium)
                    Text(label(if (recording) "Recording your product story" else "Speak about your product in your language"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (audio != null && !recording) item { OutlinedButton(viewModel::playRecordedAudio) { Text(label("Play recording")) } }
        if (!online) item { Text(label("Your photo and recording will wait safely for a connection. You will review the listing before it is published.")) }
        item { BottomActionBar("Generate listing", { viewModel.triggerGenerateListing(onNavigateToProcessing, onNavigateToCatalog) }, image != null && audio != null && !recording && !busy, label("Both a photo and a voice recording are required.")) }
    }
}
