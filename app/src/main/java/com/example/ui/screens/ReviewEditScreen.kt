package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.model.*
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable fun ReviewEditScreen(viewModel: KalaSetuViewModel, onBackToCapture: () -> Unit, onConfirmedNavigateToCatalog: () -> Unit) {
    val product by viewModel.currentDraft.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val proposal by viewModel.assistantProposal.collectAsState()
    val recording by viewModel.isRecording.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    var original by rememberSaveable { mutableStateOf(false) }
    var instruction by rememberSaveable { mutableStateOf("") }
    var voiceReady by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) viewModel.startVoiceEditRecording() }
    val p = product
    if (p == null) { Column(Modifier.padding(24.dp)) { Text(label("Select a draft to review.")); TextButton(onBackToCapture) { Text(label("Back")) } }; return }
    var price by rememberSaveable(p.id) { mutableStateOf(if (p.finalPrice > 0) p.finalPrice.toString() else "") }
    var tags by remember(p.id, p.tags) { mutableStateOf(p.tags.joinToString(", ")) }
    val issues = p.qualityIssues()
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.testTag("review_edit_screen")) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Review listing")
                SuggestionChip(onClick = {}, label = { Text(label("AI Generated")) })
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(label("Review before saving — this is AI generated"), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        item { ProductImage(p, original) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(original, { original = true }, label = { Text(label("Original")) })
            if (!p.imageWarning && p.imageUrl != p.originalImageUrl) FilterChip(!original, { original = false }, label = { Text(label("Enhanced")) })
        }
            Text(label(if (p.imageWarning) "Enhancement unavailable. The original is preserved." else "Compare the photos and make sure your product has not changed."))
        }
        item { OutlinedTextField(p.title, viewModel::updateDraftTitle, label = { Text(label("Title")) }, supportingText = { Text("${p.title.length}/80") }, isError = p.title.length > 80, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(p.description, viewModel::updateDraftDescription, label = { Text(label("Description")) }, supportingText = { Text("${p.description.length}/400") }, isError = p.description.length > 400, modifier = Modifier.fillMaxWidth()) }
        item { ChoiceMenu(p.category.displayName, CraftCategory.entries.map { it.displayName }) { viewModel.updateDraftCategory(CraftCategory.fromString(it)) } }
        item { OutlinedTextField(tags, { value -> tags = value; viewModel.updateDraftTags(value.split(',').map(String::trim).filter(String::isNotBlank)) }, label = { Text(label("Tags")) }, supportingText = { Text(label("Separate tags with commas")) }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(price, { price = it; viewModel.updateDraftPrice(it.toDoubleOrNull() ?: 0.0) }, label = { Text(label("Your price (INR)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = p.finalPrice <= 0 || !p.finalPrice.isFinite(), modifier = Modifier.fillMaxWidth()) }
        item { PriceGuidance(p) }
        if (p.attributes.isNotEmpty()) item {
            SectionTitle("Craft details")
            p.attributes.forEach { (key, value) -> OutlinedTextField(value, { viewModel.updateDraftAttributes(p.attributes + (key to it)) }, label = { Text(key) }, modifier = Modifier.fillMaxWidth()) }
        }
        if (!prefs.simpleView) {
            item { SectionTitle("AI assistant", "Suggestions are shown for approval before they change your listing.") }
            item { ChoiceMenu("Choose an action", listOf("Improve title", "Improve description", "Simplify description", "Generate tags", "Translate to selected language", "Suggest price")) { viewModel.requestAssistant(it) } }
            item { OutlinedTextField(instruction, { instruction = it.take(500) }, label = { Text(label("Describe an edit")) }, modifier = Modifier.fillMaxWidth()) }
            item { ActionButton("Propose edit", { viewModel.requestAssistant(instruction) }, instruction.isNotBlank() && !busy) }
            item { OutlinedButton({ if (recording) { viewModel.stopRecording(); voiceReady = true } else { voiceReady = false; permission.launch(Manifest.permission.RECORD_AUDIO) } }, enabled = !busy) {
                Text(if (recording) label("Stop recording") else label("Record a voice edit"))
            } }
            if (voiceReady && !recording) item { ActionButton("Propose voice edit", viewModel::requestVoiceEdit, !busy) }
        }
        if (!p.voiceTranscript.isNullOrBlank()) item { SectionTitle("Original voice note"); Text(p.voiceTranscript!!) }
        item { OutlinedButton(viewModel::readListingAloud, enabled = !busy) { Text(label("Read aloud")) } }
        item { QualityChecklist(p) }
        item { ActionButton("Confirm and publish", { viewModel.confirmListing(onConfirmedNavigateToCatalog) }, issues.isEmpty() && !busy) }
        item { OutlinedButton(viewModel::saveDraft, enabled = !busy) { Text(label("Save draft")) } }
    }
    proposal?.let { proposed -> AlertDialog(onDismissRequest = { viewModel.assistantProposal.value = null },
        title = { Text(label("Review proposed changes")) }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(proposed.title); Text(proposed.description); Text(proposed.tags.joinToString(", "))
                Text("${proposed.category.displayName} · Suggested ${money(proposed.suggestedPrice)}")
                Text("${label("Your selling price remains ")}${money(p.finalPrice)}. ${label("Change it yourself before publishing.")}")
            }
        }, confirmButton = { TextButton(viewModel::applyAssistantProposal) { Text(label("Apply proposal")) } },
        dismissButton = { TextButton({ viewModel.assistantProposal.value = null }) { Text(label("Cancel")) } }) }
}

@Composable private fun QualityChecklist(p: Product) {
    val checks = listOf(
        "Product photo" to !p.originalImageUrl.isNullOrBlank(),
        "Product title" to (p.title.isNotBlank() && p.title.length <= 80),
        "Description" to (p.description.isNotBlank() && p.description.length <= 400),
        "Category" to (p.category != CraftCategory.OTHER),
        "Price" to (p.finalPrice > 0 && p.finalPrice.isFinite())
    )
    OutlinedCard(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label(if (checks.all { it.second }) "Ready to publish" else "Needs attention"), style = MaterialTheme.typography.titleLarge)
            checks.forEach { (name, valid) -> Text("${if (valid) "✓" else "○"} ${label(name)}", color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
        }
    }
}
