package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.AppLanguage
import com.example.core.model.Product
import com.example.ui.components.LanguageSelector
import com.example.ui.components.ProductCard
import com.example.ui.components.dashedBorder
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.KalaTextMuted
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: KalaSetuViewModel,
    onNavigateToCapture: () -> Unit,
    onSelectProduct: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val products by viewModel.products.collectAsState()
    val artisanName by viewModel.artisanName.collectAsState()
    val artisanSpecialty by viewModel.artisanCraftSpecialty.collectAsState()
    val artisanLocation by viewModel.artisanLocation.collectAsState()
    val artisanPhone by viewModel.artisanPhone.collectAsState()
    val isVoiceGuideEnabled by viewModel.isVoiceGuideEnabled.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFairPriceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KalaBackground)
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                // Top Row: Localized "My Catalog" on left, Artisan initials avatar on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = KalaSetuStrings.myCatalog(currentLanguage),
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = KalaText
                        ),
                        modifier = Modifier.testTag("catalog_title")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { showSettingsSheet = true }
                            .padding(4.dp)
                            .testTag("artisan_profile_button")
                    ) {
                        Text(
                            text = artisanName.split(" ").firstOrNull() ?: artisanName,
                            style = Typography.bodyLarge.copy(
                                color = KalaText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(KalaPrimary)
                        ) {
                            Text(
                                text = artisanName.firstOrNull()?.toString()?.uppercase() ?: "A",
                                color = Color.White,
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Localized Subtitle
                Text(
                    text = KalaSetuStrings.catalogSubtitle(currentLanguage),
                    style = Typography.bodyMedium.copy(
                        color = Color(0xFF8A7A70),
                        fontSize = 14.sp
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Capture (Localized)
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCapture,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = KalaSetuStrings.navCapture(currentLanguage),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = KalaSetuStrings.navCapture(currentLanguage),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color(0xFF8A7A70),
                        unselectedTextColor = Color(0xFF8A7A70)
                    ),
                    modifier = Modifier.testTag("nav_capture_tab")
                )

                // Tab 2: Catalog (Selected & Localized)
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on catalog */ },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = KalaSetuStrings.navCatalog(currentLanguage),
                            tint = KalaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = KalaSetuStrings.navCatalog(currentLanguage),
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KalaPrimary
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KalaPrimary,
                        selectedTextColor = KalaPrimary,
                        indicatorColor = Color(0xFFF6ECE3)
                    ),
                    modifier = Modifier.testTag("nav_catalog_tab")
                )

                // Tab 3: Settings (Localized)
                NavigationBarItem(
                    selected = false,
                    onClick = { showSettingsSheet = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = KalaSetuStrings.navSettings(currentLanguage),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = KalaSetuStrings.navSettings(currentLanguage),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color(0xFF8A7A70),
                        unselectedTextColor = Color(0xFF8A7A70)
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        },
        containerColor = KalaBackground,
        modifier = Modifier.testTag("catalog_screen")
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Existing products with delete button and full localization
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onSelectProduct(product) },
                    language = currentLanguage,
                    onDeleteClick = { productToDelete = product }
                )
            }

            // "+ New Listing" Card (Localized)
            item {
                Surface(
                    onClick = onNavigateToCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .testTag("create_new_listing_grid_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .dashedBorder(
                                width = 1.5.dp,
                                color = Color(0xFFD0C5BD),
                                cornerRadius = 16.dp,
                                dashLength = 7.dp,
                                gapLength = 5.dp
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFAF5EE)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = KalaSetuStrings.newListingCard(currentLanguage),
                                tint = KalaPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = KalaSetuStrings.newListingCard(currentLanguage),
                                style = Typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KalaPrimary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Confirmation Dialog for Product Deletion (Requested by User)
        if (productToDelete != null) {
            val product = productToDelete!!
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = {
                    Text(
                        text = KalaSetuStrings.deleteConfirmationTitle(currentLanguage),
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "\"${product.title}\"",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = KalaPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = KalaSetuStrings.deleteConfirmationBody(currentLanguage),
                            style = Typography.bodyMedium.copy(color = KalaTextMuted)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(product.id)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("confirm_delete_product_button")
                    ) {
                        Text(
                            text = KalaSetuStrings.delete(currentLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text(
                            text = KalaSetuStrings.cancel(currentLanguage),
                            color = KalaText
                        )
                    }
                }
            )
        }

        // Settings Bottom Sheet with Product Deletion, Language Switcher & Unique Features
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = KalaSetuStrings.artisanSettings(currentLanguage),
                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = KalaText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Artisan Info Card with Edit button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFBF7F3))
                            .border(BorderStroke(1.dp, Color(0xFFE8DDD4)), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KalaPrimary)
                        ) {
                            Text(
                                text = artisanName.firstOrNull()?.toString()?.uppercase() ?: "A",
                                color = Color.White,
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = artisanName,
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                            )
                            Text(
                                text = "$artisanLocation • $artisanSpecialty",
                                style = Typography.bodySmall.copy(color = Color(0xFF8A7A70))
                            )
                            if (artisanPhone.isNotEmpty()) {
                                Text(
                                    text = artisanPhone,
                                    style = Typography.bodySmall.copy(color = Color(0xFF8A7A70), fontSize = 11.sp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = KalaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Language Selector Section (Entire app converts dynamically)
                    Text(
                        text = KalaSetuStrings.preferredLanguage(currentLanguage),
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = KalaText)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LanguageSelector(
                        selectedLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFF0ECE7))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. PRODUCT DELETION MANAGEMENT IN SETTINGS (User Request: "he or she can delete there product in setting")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = KalaSetuStrings.deleteProduct(currentLanguage),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                        )
                        Text(
                            text = "${products.size} ${if (currentLanguage == AppLanguage.TELUGU) "ఉత్పత్తులు" else if (currentLanguage == AppLanguage.HINDI) "उत्पाद" else "items"}",
                            style = Typography.bodySmall.copy(color = Color(0xFF8A7A70))
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFAF5EE))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = KalaSetuStrings.emptyCatalogTitle(currentLanguage),
                                style = Typography.bodyMedium.copy(color = Color(0xFF8A7A70))
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFBF7F3))
                                .border(BorderStroke(1.dp, Color(0xFFE8DDD4)), RoundedCornerShape(14.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            products.forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFE8E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (prod.localImageUri != null) {
                                            AsyncImage(
                                                model = prod.localImageUri,
                                                contentDescription = prod.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else if (prod.sampleDrawableRes != null) {
                                            Image(
                                                painter = painterResource(id = prod.sampleDrawableRes),
                                                contentDescription = prod.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_terracotta_pot_detailed),
                                                contentDescription = prod.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.title,
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = KalaText),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "₹ ${prod.finalPrice.toInt()} • ${KalaSetuStrings.localizedCategory(prod.category.displayName, currentLanguage)}",
                                            style = Typography.bodySmall.copy(color = Color(0xFF8A7A70), fontSize = 12.sp)
                                        )
                                    }

                                    // Delete Action Button
                                    IconButton(
                                        onClick = { productToDelete = prod },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEBEE))
                                            .testTag("delete_item_${prod.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete ${prod.title}",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color(0xFFF0ECE7))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. UNIQUE EXTRA FEATURES (User Request: "add some extra featuers that make my app unique")
                    Text(
                        text = if (currentLanguage == AppLanguage.TELUGU) "ప్రత్యేక ఫీచర్లు (Unique Features)"
                               else if (currentLanguage == AppLanguage.HINDI) "विशिष्ट सुविधाएं (Unique Features)"
                               else "Artisan Special Features",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Unique Feature 1: Voice Guidance Assistant (వాయిస్ గైడ్ / आवाज़ सहायता)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF7F3)),
                        border = BorderStroke(1.dp, Color(0xFFE8DDD4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = KalaPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = KalaSetuStrings.voiceGuideTitle(currentLanguage),
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                                    )
                                }
                                Switch(
                                    checked = isVoiceGuideEnabled,
                                    onCheckedChange = { viewModel.toggleVoiceGuide() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = KalaPrimary, checkedTrackColor = Color(0xFFF6ECE3))
                                )
                            }
                            Text(
                                text = KalaSetuStrings.voiceGuideSubtitle(currentLanguage),
                                style = Typography.bodySmall.copy(color = Color(0xFF8A7A70), fontSize = 12.sp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (isVoiceGuideEnabled) {
                                Button(
                                    onClick = {
                                        val tips = when (currentLanguage) {
                                            AppLanguage.TELUGU -> "నమస్తే! కళాసేతు వాయిస్ అసిస్టెంట్ ఆన్‌లో ఉంది. మీ హస్తకళల వివరాలను సులభంగా మాట్లాడి లిస్టింగ్ చేయవచ్చు."
                                            AppLanguage.HINDI -> "नमस्ते! कलासेतु आवाज़ सहायता चालू है। आप अपनी कलाकृतियों का विवरण बोलकर आसानी से दर्ज कर सकते हैं।"
                                            AppLanguage.ENGLISH -> "Namaste! KalaSetu voice guide is active. You can speak the story of your craft to automatically create listings."
                                        }
                                        viewModel.speakGuidance(tips)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary),
                                    shape = RoundedCornerShape(100.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = KalaSetuStrings.readAloud(currentLanguage),
                                        style = Typography.labelMedium.copy(color = Color.White)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unique Feature 2: Fair Craft Price Calculator (సరసమైన ధర కాలిక్యులేటర్ / उचित मूल्य कैलकुलेटर)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF7F3)),
                        border = BorderStroke(1.dp, Color(0xFFE8DDD4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFairPriceDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = KalaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = KalaSetuStrings.fairPriceTitle(currentLanguage),
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                                    )
                                    Text(
                                        text = KalaSetuStrings.fairPriceSubtitle(currentLanguage),
                                        style = Typography.bodySmall.copy(color = Color(0xFF8A7A70), fontSize = 12.sp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = KalaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Unique Feature 3: WhatsApp Catalog Share (WhatsApp కేటలాగ్ షేర్ / WhatsApp पर साझा करें)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF7F3)),
                        border = BorderStroke(1.dp, Color(0xFFE8DDD4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val shareText = KalaSetuStrings.shareCatalogMessage(artisanName, products.size, currentLanguage)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = KalaSetuStrings.shareCatalogTitle(currentLanguage),
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                                    )
                                    Text(
                                        text = KalaSetuStrings.shareCatalogSubtitle(currentLanguage),
                                        style = Typography.bodySmall.copy(color = Color(0xFF8A7A70), fontSize = 12.sp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Connectivity Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFBF7F3))
                            .clickable { viewModel.toggleOnlineStatus() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = KalaSetuStrings.connectivityStatus(currentLanguage),
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = KalaText)
                            )
                            Text(
                                text = if (isOnline) "Connected (Cloud Sync active)" else "Offline Mode (Local Storage)",
                                style = Typography.bodySmall.copy(color = Color(0xFF8A7A70))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isOnline) Color(0xFF388E3C) else Color(0xFFD4A017))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnline) KalaSetuStrings.onlineStatus(currentLanguage) else KalaSetuStrings.offlineStatus(currentLanguage),
                                style = Typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Edit Artisan Profile Dialog
        if (showEditProfileDialog) {
            var tempName by remember { mutableStateOf(artisanName) }
            var tempSpecialty by remember { mutableStateOf(artisanSpecialty) }
            var tempLocation by remember { mutableStateOf(artisanLocation) }
            var tempPhone by remember { mutableStateOf(artisanPhone) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = {
                    Text(
                        text = KalaSetuStrings.artisanProfileTitle(currentLanguage),
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tempSpecialty,
                            onValueChange = { tempSpecialty = it },
                            label = { Text("Craft Specialty") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tempLocation,
                            onValueChange = { tempLocation = it },
                            label = { Text("Location / Village") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tempPhone,
                            onValueChange = { tempPhone = it },
                            label = { Text("Contact Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateArtisanProfile(tempName, tempSpecialty, tempLocation, tempPhone)
                            showEditProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary)
                    ) {
                        Text(KalaSetuStrings.save(currentLanguage), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text(KalaSetuStrings.cancel(currentLanguage), color = KalaText)
                    }
                }
            )
        }

        // Fair Price Calculator Dialog
        if (showFairPriceDialog) {
            var rawMaterialCost by remember { mutableStateOf("250") }
            var craftingHours by remember { mutableStateOf("4") }

            val matCost = rawMaterialCost.toDoubleOrNull() ?: 0.0
            val hours = craftingHours.toDoubleOrNull() ?: 0.0
            // Hourly artisan wage ₹100/hr + 20% craft heritage premium
            val fairPrice = (matCost + (hours * 100)) * 1.2

            AlertDialog(
                onDismissRequest = { showFairPriceDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = KalaPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = KalaSetuStrings.fairPriceTitle(currentLanguage),
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = KalaSetuStrings.fairPricingNote(currentLanguage),
                            style = Typography.bodySmall.copy(color = Color(0xFF8A7A70))
                        )
                        OutlinedTextField(
                            value = rawMaterialCost,
                            onValueChange = { if (it.all { char -> char.isDigit() }) rawMaterialCost = it },
                            label = { Text(KalaSetuStrings.rawMaterialCost(currentLanguage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = craftingHours,
                            onValueChange = { if (it.all { char -> char.isDigit() }) craftingHours = it },
                            label = { Text(KalaSetuStrings.craftHours(currentLanguage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6ECE3)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = KalaSetuStrings.recommendedFairPrice(currentLanguage),
                                    style = Typography.labelMedium.copy(color = KalaPrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "₹ ${fairPrice.toInt()}",
                                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = KalaPrimary)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showFairPriceDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary)
                    ) {
                        Text(KalaSetuStrings.close(currentLanguage), color = Color.White)
                    }
                }
            )
        }
    }
}
