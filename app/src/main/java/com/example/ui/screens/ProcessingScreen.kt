package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.model.ProcessingStage
import com.example.core.localization.KalaSetuStrings
import com.example.ui.components.ProgressStepRow
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable fun ProcessingScreen(viewModel: KalaSetuViewModel, onNavigateToReview: () -> Unit, onNavigateBackToCapture: () -> Unit) {
    val stage by viewModel.processingStage.collectAsState()
    val draft by viewModel.currentDraft.collectAsState()
    val error by viewModel.processingError.collectAsState()
    val processing by viewModel.isProcessing.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val processingMessage = translated("Your listing is processing. Please wait for the result.", language)
    BackHandler(processing) { viewModel.message.value = processingMessage }
    LaunchedEffect(stage, draft) { if (stage == ProcessingStage.COMPLETED && draft != null) onNavigateToReview() }
    Column(Modifier.fillMaxSize().padding(24.dp).testTag("processing_screen"), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SectionTitle("Creating your listing", "Your photo and voice note are saved on this device.")
        if (error == null) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val steps = listOf(
                        KalaSetuStrings.stepVoice(language) to ProcessingStage.TRANSCRIBING,
                        KalaSetuStrings.stepPhoto(language) to ProcessingStage.ENHANCING_IMAGE,
                        KalaSetuStrings.stepCraft(language) to ProcessingStage.CATEGORISING,
                        KalaSetuStrings.stepListing(language) to ProcessingStage.GENERATING_LISTING
                    )
                    steps.forEachIndexed { index, (title, step) ->
                        ProgressStepRow(index + 1, title, step == stage, step.stepNumber < stage.stepNumber || stage == ProcessingStage.COMPLETED, index == steps.lastIndex, language = language)
                    }
                }
            }
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(label("This can take a few minutes on a slow connection."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            ActionButton("Retry", viewModel::executeProcessingPipeline, !processing)
            OutlinedButton(onNavigateBackToCapture) { Text(label("Return to capture")) }
        }
    }
}
