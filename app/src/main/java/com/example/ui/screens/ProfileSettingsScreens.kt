package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.core.model.*
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable fun ProfileScreen(vm: KalaSetuViewModel, settings: () -> Unit) {
    val saved by vm.profile.collectAsState()
    val language by vm.currentLanguage.collectAsState()
    val profileSavedMessage = translated("Profile saved. Future publishing uses these details.", language)
    var profile by remember { mutableStateOf(saved) }
    // A photo picker updates only the photo; keep text the artisan is still editing.
    LaunchedEffect(saved.photo) { profile = profile.copy(photo = saved.photo) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { vm.setProfilePhoto(it.toString()) } }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionTitle("Profile", "Give your craft a name and a story.") }
        item {
            if (profile.photo.isNotBlank()) AsyncImage(profile.photo, label("Profile photo"), modifier = Modifier.size(96.dp))
            OutlinedButton({ picker.launch("image/*") }) { Text(label("Choose photo")) }
        }
        item { ProfileField("Display name", profile.display_name, 80) { profile = profile.copy(display_name = it) } }
        item { ProfileField("Shop name", profile.shop_name, 80) { profile = profile.copy(shop_name = it) } }
        item { ProfileField("Craft specialization", profile.craft, 80) { profile = profile.copy(craft = it) } }
        item { ProfileField("Location", profile.location, 120) { profile = profile.copy(location = it) } }
        item { ProfileField("Short bio", profile.bio, 400) { profile = profile.copy(bio = it) } }
        item { ProfileField("Phone", profile.contact, 100) { profile = profile.copy(contact = it.filter { char -> char.isDigit() || char in "+ -()" }) } }
        item { ProfileField("Email", profile.email, 100) { profile = profile.copy(email = it) } }
        item { SettingToggle("Show my phone to buyers", profile.contact_public) { profile = profile.copy(contact_public = it) } }
        item { Text(label("Your name, shop, craft, location and bio are included when you publish. Phone is shared only with your permission. Email and profile photo remain on this device.")) }
        item { ActionButton("Save", { vm.updateProfile(profile); vm.message.value = profileSavedMessage }) }
        item { ActionButton("Settings", settings) }
    }
}
@Composable fun ProfileField(title: String, value: String, max: Int, onChange: (String) -> Unit) {
    OutlinedTextField(value, { if (it.length <= max) onChange(it) }, label = { Text(label(title)) }, modifier = Modifier.fillMaxWidth())
}
@Composable fun SettingToggle(title: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label(title), modifier = Modifier.weight(1f).padding(end = 12.dp)); Switch(checked, change)
    }
}
@Composable fun SettingsScreen(vm: KalaSetuViewModel, navigate: (String) -> Unit) {
    val prefs by vm.preferences.collectAsState()
    val language by vm.currentLanguage.collectAsState()
    val voice by vm.isVoiceGuideEnabled.collectAsState()
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionTitle("Settings", "Make KalaSetu comfortable for you.") }
        item { SectionTitle("Language") }
        item { ChoiceMenu(language.nativeName, AppLanguage.entries.map { it.nativeName }) { name -> vm.setLanguage(AppLanguage.entries.first { it.nativeName == name }) } }
        item { SectionTitle("Appearance") }
        item { ChoiceMenu(prefs.theme, listOf("Light", "Dark", "System")) { vm.updatePreferences(prefs.copy(theme = it)) } }
        item { SettingToggle("Simple view", prefs.simpleView) { vm.updatePreferences(prefs.copy(simpleView = it)) } }
        item { SettingToggle("Larger text", prefs.largerText) { vm.updatePreferences(prefs.copy(largerText = it)) } }
        item { SettingToggle("High contrast", prefs.highContrast) { vm.updatePreferences(prefs.copy(highContrast = it)) } }
        item { SettingToggle("Voice guidance", voice) { vm.toggleVoiceGuide() } }
        item { Text("${label("Speech speed: ")}${"%.1f".format(prefs.speechSpeed)}×")
            Slider(prefs.speechSpeed, { vm.updatePreferences(prefs.copy(speechSpeed = it)) }, valueRange = 0.5f..1.5f, steps = 3) }
        item { ChoiceMenu(AppLanguage.entries.find { it.code == prefs.ttsLanguage }?.nativeName ?: label("Follow app language"),
            listOf("Follow app language") + AppLanguage.entries.map { it.nativeName }) { name ->
            vm.updatePreferences(prefs.copy(ttsLanguage = AppLanguage.entries.find { it.nativeName == name }?.code ?: "auto"))
        } }
        item { SectionTitle("Notifications") }
        item { SettingToggle("Listing ready for review", prefs.processingNotifications) { vm.updatePreferences(prefs.copy(processingNotifications = it)) } }
        item { SettingToggle("Sync completed", prefs.syncNotifications) { vm.updatePreferences(prefs.copy(syncNotifications = it)) } }
        item { SettingToggle("Listing errors", prefs.errorNotifications) { vm.updatePreferences(prefs.copy(errorNotifications = it)) } }
        item { OutlinedButton({ if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text(label("Allow device notifications")) } }
        item { ActionButton("Sync center", { navigate("sync") }) }
        item { SectionTitle("Privacy") }
        item { Text(label("Photos and recordings are stored privately on this device. Processing sends them to the backend and configured AI services. Original product photos are stored in cloud storage; product images may be publicly accessible by URL. Audio is deleted from backend temporary storage after processing. Published listings expose only the public profile you chose. Uninstalling clears local drafts, preferences and the installation key used to manage your cloud listings.")) }
        item { SectionTitle("Help") }
        item { Text(label("How it works\n1. Take or choose a photo.\n2. Record up to 29 seconds about your craft.\n3. Review AI suggestions, photos and price.\n4. Confirm to publish.\n\nNo connection? Your capture waits safely in the sync center. Failed work can be retried. AI may be wrong; always check material, size and price.")) }
        item { OutlinedButton({
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT,
                "KalaSetu ${BuildConfig.VERSION_NAME} issue report\nWhat happened:\nSteps to reproduce:\nExpected result:") }
            context.startActivity(Intent.createChooser(intent, "Report a problem"))
        }) { Text(label("Report a problem")) } }
        item { Text(label("A support address has not been configured. Share the issue report with your project contact.")) }
        item { SectionTitle("About") }
        item { Text("KalaSetu ${BuildConfig.VERSION_NAME}\n${label("An AI catalog and discovery assistant for Indian artisans.")}\nProject: SIH26090 · KalaSetu team") }
    }
}
