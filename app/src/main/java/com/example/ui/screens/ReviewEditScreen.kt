package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable fun ReviewEditScreen(
    viewModel: KalaSetuViewModel,
    onBackToCapture: () -> Unit,
    onConfirmedNavigateToCatalog: () -> Unit
) {
    val product by viewModel.currentDraft.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val proposal by viewModel.assistantProposal.collectAsState()
    val recording by viewModel.isRecording.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    var original by rememberSaveable { mutableStateOf(false) }
    var instruction by rememberSaveable { mutableStateOf("") }
    var voiceReady by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) viewModel.startVoiceEditRecording()
    }
    val p = product
    if (p == null) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(label("Select a draft to review."), style = Typography.titleMedium)
            Button(onClick = onBackToCapture, shape = RoundedCornerShape(100.dp)) {
                Text(label("Back"))
            }
        }
        return
    }
    var price by rememberSaveable(p.id) { mutableStateOf(if (p.finalPrice > 0) p.finalPrice.toString() else "") }
    var tags by remember(p.id, p.tags) { mutableStateOf(p.tags.joinToString(", ")) }
    val issues = p.qualityIssues()

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.testTag("review_edit_screen")
    ) {
        // Screen Title Bar
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Review listing")
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                        Text(
                            text = label("AI Generated"),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        )
                    }
                }
            }
        }

        // Amber Notice Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = KalaBanner,
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFFC2410C), modifier = Modifier.size(24.dp))
                    Text(
                        text = label("Review before saving — this is AI generated"),
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF9A3412))
                    )
                }
            }
        }

        // Product Photo with Original / Enhanced Filter Chips
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProductImage(p, original)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = original,
                                onClick = { original = true },
                                label = { Text(label("Original"), fontWeight = if (original) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalaPrimary, selectedLabelColor = Color.White)
                            )
                            if (!p.imageWarning && p.imageUrl != p.originalImageUrl) {
                                FilterChip(
                                    selected = !original,
                                    onClick = { original = false },
                                    label = { Text(label("Enhanced"), fontWeight = if (!original) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(100.dp),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalaPrimary, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }

                    Text(
                        text = label(if (p.imageWarning) "Enhancement unavailable. The original is preserved." else "Compare the photos and make sure your product has not changed."),
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Details Form Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(label("Product details"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = p.title,
                        onValueChange = viewModel::updateDraftTitle,
                        label = { Text(label("Title")) },
                        supportingText = { Text("${p.title.length}/80") },
                        isError = p.title.length > 80,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = p.description,
                        onValueChange = viewModel::updateDraftDescription,
                        label = { Text(label("Description")) },
                        supportingText = { Text("${p.description.length}/400") },
                        isError = p.description.length > 400,
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label("Category"), style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        ChoiceMenu(p.category.displayName, CraftCategory.entries.map { it.displayName }) {
                            viewModel.updateDraftCategory(CraftCategory.fromString(it))
                        }
                    }

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { value ->
                            tags = value
                            viewModel.updateDraftTags(value.split(',').map(String::trim).filter(String::isNotBlank))
                        },
                        label = { Text(label("Tags")) },
                        supportingText = { Text(label("Separate tags with commas")) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            viewModel.updateDraftPrice(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(label("Your price (INR)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = p.finalPrice <= 0 || !p.finalPrice.isFinite(),
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = KalaPrimary) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Market Price Guidance
        item { PriceGuidance(p) }

        if (p.attributes.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("Craft details")
                        p.attributes.forEach { (key, value) ->
                            OutlinedTextField(
                                value = value,
                                onValueChange = { viewModel.updateDraftAttributes(p.attributes + (key to it)) },
                                label = { Text(key) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // AI Assistant Studio Card
        if (!prefs.simpleView) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = KalaPrimary)
                            Text(label("AI assistant"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(
                            text = label("Suggestions are shown for approval before they change your listing."),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ChoiceMenu(
                            selected = "Choose an action",
                            options = listOf("Improve title", "Improve description", "Simplify description", "Generate tags", "Translate to selected language", "Suggest price")
                        ) { viewModel.requestAssistant(it) }

                        OutlinedTextField(
                            value = instruction,
                            onValueChange = { instruction = it.take(500) },
                            label = { Text(label("Describe an edit")) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.requestAssistant(instruction) },
                                enabled = instruction.isNotBlank() && !busy,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text(label("Propose edit"), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (recording) {
                                        viewModel.stopRecording()
                                        voiceReady = true
                                    } else {
                                        voiceReady = false
                                        permission.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                enabled = !busy,
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Icon(if (recording) Icons.Outlined.Stop else Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (recording) label("Stop recording") else label("Record a voice edit"), fontSize = 12.sp)
                            }
                        }

                        if (voiceReady && !recording) {
                            ActionButton("Propose voice edit", viewModel::requestVoiceEdit, !busy)
                        }
                    }
                }
            }
        }

        if (!p.voiceTranscript.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionTitle("Original voice note")
                        Text(p.voiceTranscript!!, style = Typography.bodyMedium)
                    }
                }
            }
        }

        // Read Aloud Button
        item {
            OutlinedButton(
                onClick = viewModel::readListingAloud,
                enabled = !busy,
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp), tint = KalaPrimary)
                Spacer(Modifier.width(8.dp))
                Text(label("Read aloud"), fontWeight = FontWeight.Bold, color = KalaPrimary)
            }
        }

        // Quality Checklist
        item { QualityChecklist(p) }

        // Publish and Draft Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("Confirm and publish", { viewModel.confirmListing(onConfirmedNavigateToCatalog) }, issues.isEmpty() && !busy)
                OutlinedButton(
                    onClick = viewModel::saveDraft,
                    enabled = !busy,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(label("Save draft"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    proposal?.let { proposed ->
        AlertDialog(
            onDismissRequest = { viewModel.assistantProposal.value = null },
            title = { Text(label("Review proposed changes"), style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(proposed.title, fontWeight = FontWeight.Bold)
                    Text(proposed.description)
                    Text(proposed.tags.joinToString(", "), color = KalaPrimary)
                    Text("${proposed.category.displayName} · Suggested ${money(proposed.suggestedPrice)}")
                    Text("${label("Your selling price remains ")}${money(p.finalPrice)}. ${label("Change it yourself before publishing.")}")
                }
            },
            confirmButton = {
                Button(onClick = viewModel::applyAssistantProposal, shape = RoundedCornerShape(100.dp), colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary)) {
                    Text(label("Apply proposal"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.assistantProposal.value = null }) {
                    Text(label("Cancel"))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable private fun QualityChecklist(p: Product) {
    val checks = listOf(
        "Product photo" to !p.originalImageUrl.isNullOrBlank(),
        "Product title" to (p.title.isNotBlank() && p.title.length <= 80),
        "Description" to (p.description.isNotBlank() && p.description.length <= 400),
        "Category" to (p.category != CraftCategory.OTHER),
        "Price" to (p.finalPrice > 0 && p.finalPrice.isFinite())
    )
    val allReady = checks.all { it.second }
    val readyCount = checks.count { it.second }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (allReady) Color(0xFFA5D6A7) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label(if (allReady) "Ready to publish" else "Needs attention"),
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (allReady) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (allReady) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "$readyCount/5",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (allReady) Color(0xFF15803D) else Color(0xFFB45309))
                    )
                }
            }

            checks.forEach { (name, valid) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (valid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (valid) Icons.Outlined.Check else Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = if (valid) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${if (valid) "✓" else "○"} ${label(name)}",
                        style = Typography.bodyMedium.copy(
                            fontWeight = if (valid) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (valid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}
