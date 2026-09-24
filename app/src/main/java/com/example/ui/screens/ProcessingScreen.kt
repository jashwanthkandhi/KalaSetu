package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.ProcessingStage
import com.example.ui.components.ProgressStepRow
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable
fun ProcessingScreen(
    viewModel: KalaSetuViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateBackToCapture: () -> Unit
) {
    val currentStage by viewModel.processingStage.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    val draftProduct by viewModel.currentDraft.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // When completed, auto-navigate to review
    LaunchedEffect(currentStage, draftProduct) {
        if (currentStage == ProcessingStage.COMPLETED && draftProduct != null) {
            onNavigateToReview()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KalaBackground)
            .testTag("processing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Centered Header "KalaSetu"
            Text(
                text = "KalaSetu",
                style = Typography.headlineMedium.copy(
                    color = Color(0xFF2C1810),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Photo Preview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = KalaSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.25f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8F4F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (capturedImageUri != null && !capturedImageUri!!.contains("ic_terracotta_pot_detailed")) {
                            AsyncImage(
                                model = capturedImageUri,
                                contentDescription = "Captured Product",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_terracotta_pot_detailed),
                                contentDescription = "Craft Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = KalaSetuStrings.aiEnhancingPhoto(currentLanguage),
                        style = Typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF6D5E57),
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 2: 4-Step Stepper Progress Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = KalaSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Step 1: Understanding your voice
                    ProgressStepRow(
                        stepNumber = 1,
                        title = KalaSetuStrings.stepVoice(currentLanguage),
                        isActive = currentStage == ProcessingStage.TRANSCRIBING,
                        isCompleted = currentStage.stepNumber > 1,
                        language = currentLanguage
                    )

                    // Step 2: Enhancing your photo
                    ProgressStepRow(
                        stepNumber = 2,
                        title = KalaSetuStrings.stepPhoto(currentLanguage),
                        isActive = currentStage == ProcessingStage.ENHANCING_IMAGE,
                        isCompleted = currentStage.stepNumber > 2,
                        language = currentLanguage
                    )

                    // Step 3: Identifying your craft
                    ProgressStepRow(
                        stepNumber = 3,
                        title = KalaSetuStrings.stepCraft(currentLanguage),
                        isActive = currentStage == ProcessingStage.CATEGORISING,
                        isCompleted = currentStage.stepNumber > 3,
                        language = currentLanguage
                    )

                    // Step 4: Creating your listing
                    ProgressStepRow(
                        stepNumber = 4,
                        title = KalaSetuStrings.stepListing(currentLanguage),
                        isActive = currentStage == ProcessingStage.GENERATING_LISTING,
                        isCompleted = currentStage.stepNumber > 4,
                        isLast = true,
                        language = currentLanguage
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "This takes about 20 seconds"
            Text(
                text = KalaSetuStrings.processingDuration(currentLanguage),
                style = Typography.bodyMedium.copy(
                    color = Color(0xFF2C1810),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Terracotta 3-dot pulse: • • •
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = dotAlpha1 }
                        .clip(CircleShape)
                        .background(KalaPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = dotAlpha2 }
                        .clip(CircleShape)
                        .background(KalaPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = dotAlpha3 }
                        .clip(CircleShape)
                        .background(KalaPrimary)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
