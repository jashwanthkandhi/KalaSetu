package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.Product
import com.example.ui.components.PrimaryButton
import com.example.ui.components.StatusChip
import com.example.ui.components.TagChip
import com.example.ui.navigation.KalaSetuDestinations
import com.example.ui.screens.CaptureScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.ReviewEditScreen
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaBorder
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSecondary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.KalaTextMuted
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KalaSetuApp(
    viewModel: KalaSetuViewModel = viewModel()
) {
    val navController = rememberNavController()
    val tutorialSeen by viewModel.tutorialSeen.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    val startDestination = if (tutorialSeen) {
        KalaSetuDestinations.CATALOG
    } else {
        KalaSetuDestinations.ONBOARDING
    }

    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(KalaSetuDestinations.ONBOARDING) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onStartCreating = {
                        navController.navigate(KalaSetuDestinations.CAPTURE) {
                            popUpTo(KalaSetuDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(KalaSetuDestinations.CAPTURE) {
                CaptureScreen(
                    viewModel = viewModel,
                    onNavigateToProcessing = {
                        navController.navigate(KalaSetuDestinations.PROCESSING)
                    },
                    onNavigateToCatalog = {
                        navController.navigate(KalaSetuDestinations.CATALOG) {
                            popUpTo(KalaSetuDestinations.CAPTURE) { inclusive = false }
                        }
                    }
                )
            }

            composable(KalaSetuDestinations.PROCESSING) {
                ProcessingScreen(
                    viewModel = viewModel,
                    onNavigateToReview = {
                        navController.navigate(KalaSetuDestinations.REVIEW) {
                            popUpTo(KalaSetuDestinations.PROCESSING) { inclusive = true }
                        }
                    },
                    onNavigateBackToCapture = {
                        navController.popBackStack()
                    }
                )
            }

            composable(KalaSetuDestinations.REVIEW) {
                ReviewEditScreen(
                    viewModel = viewModel,
                    onBackToCapture = {
                        navController.popBackStack()
                    },
                    onConfirmedNavigateToCatalog = {
                        navController.navigate(KalaSetuDestinations.CATALOG) {
                            popUpTo(KalaSetuDestinations.CAPTURE) { inclusive = true }
                        }
                    }
                )
            }

            composable(KalaSetuDestinations.CATALOG) {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToCapture = {
                        navController.navigate(KalaSetuDestinations.CAPTURE)
                    },
                    onSelectProduct = { product ->
                        selectedProductForDetail = product
                    }
                )
            }
        }

        // Product Detail Bottom Sheet
        if (selectedProductForDetail != null) {
            val product = selectedProductForDetail!!
            ModalBottomSheet(
                onDismissRequest = { selectedProductForDetail = null },
                sheetState = sheetState,
                containerColor = KalaSurface,
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = product.status)
                        IconButton(onClick = {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                selectedProductForDetail = null
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = KalaTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KalaBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        if (product.localImageUri != null) {
                            AsyncImage(
                                model = product.localImageUri,
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (product.sampleDrawableRes != null) {
                            Image(
                                painter = painterResource(id = product.sampleDrawableRes),
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_craft_pottery),
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = product.title,
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = KalaText)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.category.displayName,
                        style = Typography.bodyMedium.copy(color = KalaPrimary, fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PRICE",
                                style = Typography.labelSmall.copy(color = KalaTextMuted, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "₹${product.finalPrice.toInt()}",
                                style = Typography.headlineLarge.copy(color = KalaText, fontWeight = FontWeight.Bold)
                            )
                        }

                        if (product.suggestedPrice != product.finalPrice) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "AI ESTIMATE",
                                    style = Typography.labelSmall.copy(color = KalaTextMuted, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "₹${product.suggestedPrice.toInt()}",
                                    style = Typography.headlineSmall.copy(color = KalaSecondary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DESCRIPTION",
                        style = Typography.labelSmall.copy(color = KalaTextMuted, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = Typography.bodyLarge.copy(color = KalaText, lineHeight = 22.sp)
                    )

                    if (product.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TAGS",
                            style = Typography.labelSmall.copy(color = KalaTextMuted, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.tags.forEach { tag ->
                                TagChip(text = tag)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
