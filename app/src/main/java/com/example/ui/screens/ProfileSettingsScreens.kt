package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.core.model.*
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaPrimaryLight
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable
fun ProfileScreen(vm: KalaSetuViewModel, settings: () -> Unit) {
    val saved by vm.profile.collectAsState()
    val language by vm.currentLanguage.collectAsState()
    val profileSavedMessage = translated("Profile saved. Future publishing uses these details.", language)
    var profile by remember { mutableStateOf(saved) }
    
    // A photo picker updates only the photo; keep text the artisan is still editing.
    LaunchedEffect(saved.photo) { profile = profile.copy(photo = saved.photo) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.setProfilePhoto(it.toString()) }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionTitle("Profile", "Give your craft a name and a story.")
        }

        // Artisan Identity Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        if (profile.photo.isNotBlank()) {
                            AsyncImage(
                                model = profile.photo,
                                contentDescription = label("Profile photo"),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    )
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = label("Profile photo"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        // Camera edit badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { picker.launch("image/*") }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = label("Choose photo"),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = profile.display_name.ifBlank { "Artisan" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (profile.craft.isNotBlank() || profile.location.isNotBlank()) {
                        Text(
                            text = listOf(profile.craft, profile.location).filter { it.isNotBlank() }.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { picker.launch("image/*") },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(label("Choose photo"), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Studio & Craft Details Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = label("Studio Details"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    ProfileField("Display name", profile.display_name, 80) { profile = profile.copy(display_name = it) }
                    ProfileField("Shop name", profile.shop_name, 80) { profile = profile.copy(shop_name = it) }
                    ProfileField("Craft specialization", profile.craft, 80) { profile = profile.copy(craft = it) }
                    ProfileField("Location", profile.location, 120) { profile = profile.copy(location = it) }
                    ProfileField("Short bio", profile.bio, 400) { profile = profile.copy(bio = it) }
                }
            }
        }

        // Contact & Visibility Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = label("Contact & Visibility"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    ProfileField("Phone", profile.contact, 100) {
                        profile = profile.copy(contact = it.filter { char -> char.isDigit() || char in "+ -()" })
                    }
                    ProfileField("Email", profile.email, 100) { profile = profile.copy(email = it) }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    SettingToggle("Show my phone to buyers", profile.contact_public) {
                        profile = profile.copy(contact_public = it)
                    }

                    // Disclaimer callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Text(
                                text = label("Your name, shop, craft, location and bio are included when you publish. Phone is shared only with your permission. Email and profile photo remain on this device."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("Save", {
                    vm.updateProfile(profile)
                    vm.message.value = profileSavedMessage
                })
                ActionButton("Settings", settings)
            }
        }
    }
}

@Composable
fun ProfileField(title: String, value: String, max: Int, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= max) onChange(it) },
        label = { Text(label(title)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = max <= 120,
        maxLines = if (max <= 120) 1 else 4,
        supportingText = {
            if (max > 100) {
                Text(
                    text = "${value.length}/$max",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun SettingToggle(title: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label(title),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = change,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsScreen(vm: KalaSetuViewModel, navigate: (String) -> Unit) {
    val prefs by vm.preferences.collectAsState()
    val language by vm.currentLanguage.collectAsState()
    val voice by vm.isVoiceGuideEnabled.collectAsState()
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionTitle("Settings", "Make KalaSetu comfortable for you.")
        }

        // Language Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Language")
                    ChoiceMenu(language.nativeName, AppLanguage.entries.map { it.nativeName }) { name ->
                        vm.setLanguage(AppLanguage.entries.first { it.nativeName == name })
                    }
                }
            }
        }

        // Appearance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Appearance")
                    ChoiceMenu(prefs.theme, listOf("Light", "Dark", "System")) {
                        vm.updatePreferences(prefs.copy(theme = it))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingToggle("Simple view", prefs.simpleView) { vm.updatePreferences(prefs.copy(simpleView = it)) }
                    SettingToggle("Larger text", prefs.largerText) { vm.updatePreferences(prefs.copy(largerText = it)) }
                    SettingToggle("High contrast", prefs.highContrast) { vm.updatePreferences(prefs.copy(highContrast = it)) }
                }
            }
        }

        // Voice Guidance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = label("Voice & Audio"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SettingToggle("Voice guidance", voice) { vm.toggleVoiceGuide() }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label("Speech speed: "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "${"%.1f".format(prefs.speechSpeed)}×",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Slider(
                        value = prefs.speechSpeed,
                        onValueChange = { vm.updatePreferences(prefs.copy(speechSpeed = it)) },
                        valueRange = 0.5f..1.5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ChoiceMenu(
                        selected = AppLanguage.entries.find { it.code == prefs.ttsLanguage }?.nativeName ?: label("Follow app language"),
                        options = listOf("Follow app language") + AppLanguage.entries.map { it.nativeName }
                    ) { name ->
                        vm.updatePreferences(prefs.copy(ttsLanguage = AppLanguage.entries.find { it.nativeName == name }?.code ?: "auto"))
                    }
                }
            }
        }

        // Notifications Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Notifications")
                    SettingToggle("Listing ready for review", prefs.processingNotifications) {
                        vm.updatePreferences(prefs.copy(processingNotifications = it))
                    }
                    SettingToggle("Sync completed", prefs.syncNotifications) {
                        vm.updatePreferences(prefs.copy(syncNotifications = it))
                    }
                    SettingToggle("Listing errors", prefs.errorNotifications) {
                        vm.updatePreferences(prefs.copy(errorNotifications = it))
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label("Allow device notifications"), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Sync Center Shortcut Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ActionButton("Sync center", { navigate("sync") })
                }
            }
        }

        // Privacy Policy Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Privacy")
                    Text(
                        text = label("Photos and recordings are stored privately on this device. Processing sends them to the backend and configured AI services. Original product photos are stored in cloud storage; product images may be publicly accessible by URL. Audio is deleted from backend temporary storage after processing. Published listings expose only the public profile you chose. Uninstalling clears local drafts, preferences and the installation key used to manage your cloud listings."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Help & Support Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Help")
                    Text(
                        text = label("How it works\n1. Take or choose a photo.\n2. Record up to 29 seconds about your craft.\n3. Review AI suggestions, photos and price.\n4. Confirm to publish.\n\nNo connection? Your capture waits safely in the sync center. Failed work can be retried. AI may be wrong; always check material, size and price."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "KalaSetu ${BuildConfig.VERSION_NAME} issue report\nWhat happened:\nSteps to reproduce:\nExpected result:"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Report a problem"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(label("Report a problem"), fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = label("A support address has not been configured. Share the issue report with your project contact."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // About Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionTitle("About")
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "KS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "KalaSetu ${BuildConfig.VERSION_NAME}\n" + label("An AI catalog and discovery assistant for Indian artisans.") + "\nProject: SIH26090 · KalaSetu team",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
