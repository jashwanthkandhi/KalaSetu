package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.CraftCategory
import com.example.ui.components.BottomActionBar
import com.example.ui.components.TagChip
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewEditScreen(
    viewModel: KalaSetuViewModel,
    onBackToCapture: () -> Unit,
    onConfirmedNavigateToCatalog: () -> Unit
) {
    val draftProduct by viewModel.currentDraft.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }

    val product = draftProduct ?: return

    var editedTitle by remember(product.title) { mutableStateOf(product.title) }
    var editedDescription by remember(product.description) { mutableStateOf(product.description) }
    var newTagText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(KalaBackground)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCapture,
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
                    text = KalaSetuStrings.reviewListing(currentLanguage),
                    style = Typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = KalaText
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // AI Generated Pill (solid terracotta with white text)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(KalaPrimary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = KalaSetuStrings.aiGeneratedChip(currentLanguage),
                        style = Typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        },
        bottomBar = {
            BottomActionBar(
                buttonText = KalaSetuStrings.saveListing(currentLanguage),
                onButtonClick = {
                    viewModel.confirmListing(onComplete = onConfirmedNavigateToCatalog)
                },
                footnote = KalaSetuStrings.artisanControlFootnote(currentLanguage),
                testTag = "save_listing_button"
            )
        },
        containerColor = KalaBackground,
        modifier = Modifier.testTag("review_edit_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Notice Banner: "Review before saving — this is AI generated"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFDECDA))
                    .border(BorderStroke(1.dp, Color(0xFFF4C9A0)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2C1810),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = KalaSetuStrings.aiGeneratedBanner(currentLanguage),
                    style = Typography.bodyMedium.copy(
                        color = Color(0xFF2C1810),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Card with "Edit" pill overlay
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KalaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (product.localImageUri != null && !product.localImageUri.contains("ic_terracotta_pot_detailed")) {
                        AsyncImage(
                            model = product.localImageUri,
                            contentDescription = product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_terracotta_pot_detailed),
                            contentDescription = product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Top-right Edit button
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clickable { onBackToCapture() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit photo",
                                tint = Color(0xFF2C1810),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = KalaSetuStrings.edit(currentLanguage),
                                style = Typography.labelSmall.copy(
                                    color = Color(0xFF2C1810),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Details Card with dividers
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KalaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. TITLE ROW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditTitleDialog = true }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = KalaSetuStrings.title(currentLanguage),
                                style = Typography.labelMedium.copy(
                                    color = KalaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Title",
                                tint = Color(0xFF8A7A70),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.title,
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = KalaText
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEFE8E2))
                    )

                    // 2. DESCRIPTION ROW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditDescriptionDialog = true }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = KalaSetuStrings.description(currentLanguage),
                                style = Typography.labelMedium.copy(
                                    color = KalaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Description",
                                tint = Color(0xFF8A7A70),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = Typography.bodyMedium.copy(
                                color = KalaText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEFE8E2))
                    )

                    // 3. CATEGORY ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryMenu = true }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = KalaSetuStrings.category(currentLanguage),
                            style = Typography.bodyLarge.copy(
                                color = KalaText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFF6ECE3))
                                    .border(BorderStroke(1.dp, KalaPrimary.copy(alpha = 0.5f)), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_craft_pottery),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = KalaSetuStrings.localizedCategory(product.category.displayName, currentLanguage),
                                    style = Typography.bodyMedium.copy(
                                        color = KalaPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                CraftCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(KalaSetuStrings.localizedCategory(cat.displayName, currentLanguage)) },
                                        onClick = {
                                            viewModel.updateDraftCategory(cat)
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEFE8E2))
                    )

                    // 4. TAGS ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = KalaSetuStrings.tags(currentLanguage),
                            style = Typography.bodyLarge.copy(
                                color = KalaText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            product.tags.forEach { tag ->
                                TagChip(text = tag)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEFE8E2))
                    )

                    // 5. PRICING ROW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = KalaSetuStrings.aiSuggestedPrice(currentLanguage),
                                    style = Typography.bodyMedium.copy(
                                        color = KalaText,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF0ECE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = KalaSetuStrings.aiSuggested(currentLanguage),
                                        style = Typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = Color(0xFF6D5E57)
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "₹ ${product.suggestedPrice.toInt()}",
                                style = Typography.titleMedium.copy(
                                    color = Color(0xFFD4A017),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = KalaSetuStrings.yourPrice(currentLanguage),
                                style = Typography.bodyMedium.copy(
                                    color = KalaText,
                                    fontSize = 14.sp
                                )
                            )

                            // Price editable box with line underneath
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(bottom = 2.dp)
                            ) {
                                Text(
                                    text = "₹ ",
                                    style = Typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = KalaText
                                    )
                                )

                                var priceString by remember(product.finalPrice) {
                                    mutableStateOf(product.finalPrice.toInt().toString())
                                }

                                BasicTextField(
                                    value = priceString,
                                    onValueChange = { newVal ->
                                        if (newVal.all { it.isDigit() } && newVal.length <= 6) {
                                            priceString = newVal
                                            newVal.toDoubleOrNull()?.let { viewModel.updateDraftPrice(it) }
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KalaText
                                    ),
                                    cursorBrush = SolidColor(KalaPrimary),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Underline for price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(1.5.dp)
                                    .background(Color(0xFFD0C5BD))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Read Aloud Action (🔊 Read Aloud in terracotta)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { viewModel.readListingAloud() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Read Aloud",
                    tint = KalaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = KalaSetuStrings.readAloud(currentLanguage),
                    style = Typography.titleMedium.copy(
                        color = KalaPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dialog for editing Title
        if (showEditTitleDialog) {
            AlertDialog(
                onDismissRequest = { showEditTitleDialog = false },
                title = { Text("${KalaSetuStrings.edit(currentLanguage)} ${KalaSetuStrings.title(currentLanguage)}") },
                text = {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateDraftTitle(editedTitle)
                        showEditTitleDialog = false
                    }) {
                        Text(KalaSetuStrings.save(currentLanguage), color = KalaPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTitleDialog = false }) {
                        Text(KalaSetuStrings.cancel(currentLanguage))
                    }
                }
            )
        }

        // Dialog for editing Description
        if (showEditDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showEditDescriptionDialog = false },
                title = { Text("${KalaSetuStrings.edit(currentLanguage)} ${KalaSetuStrings.description(currentLanguage)}") },
                text = {
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateDraftDescription(editedDescription)
                        showEditDescriptionDialog = false
                    }) {
                        Text(KalaSetuStrings.save(currentLanguage), color = KalaPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDescriptionDialog = false }) {
                        Text(KalaSetuStrings.cancel(currentLanguage))
                    }
                }
            )
        }
    }
}
