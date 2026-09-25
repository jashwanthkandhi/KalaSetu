package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.core.model.AppLanguage
import com.example.core.model.ListingStatus
import com.example.core.model.Product
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaBorder
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaPrimaryDark
import com.example.ui.theme.KalaSecondary
import com.example.ui.theme.KalaSuccess
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.KalaTextMuted
import com.example.ui.theme.KalaWarning
import com.example.ui.theme.Typography
import kotlin.math.sin

/**
 * Custom Modifier to draw dashed rounded border
 */
fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp = 0.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val strokeWidthPx = width.toPx()
        val dashLengthPx = dashLength.toPx()
        val gapLengthPx = gapLength.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        val halfStroke = strokeWidthPx / 2
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = halfStroke,
                    top = halfStroke,
                    right = size.width - halfStroke,
                    bottom = size.height - halfStroke,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dashLengthPx, gapLengthPx),
                    0f
                )
            )
        )
    }
)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    testTag: String = "primary_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                val s = if (isPressed) 0.98f else 1.0f
                scaleX = s
                scaleY = s
            }
            .testTag(testTag),
        shape = RoundedCornerShape(100.dp),
        color = if (enabled) KalaPrimary else KalaPrimary.copy(alpha = 0.45f),
        shadowElevation = if (enabled && !isPressed) 4.dp else 0.dp,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = Typography.labelLarge.copy(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    testTag: String = "secondary_button"
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .testTag(testTag),
        shape = RoundedCornerShape(100.dp),
        color = KalaSurface,
        border = BorderStroke(1.5.dp, if (enabled) KalaPrimary else KalaPrimary.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = text,
                style = Typography.labelLarge.copy(
                    color = if (enabled) KalaPrimary else KalaPrimary.copy(alpha = 0.4f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = KalaText,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            color = KalaText,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        trailing?.invoke()
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val languages = listOf(AppLanguage.TELUGU, AppLanguage.HINDI, AppLanguage.ENGLISH)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        languages.forEach { lang ->
            val isSelected = lang == selectedLanguage
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) KalaPrimary else KalaSurface,
                label = "lang_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else KalaTextMuted,
                label = "lang_text"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(if (isCompact) 32.dp else 40.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(bgColor)
                    .border(
                        BorderStroke(1.dp, if (isSelected) Color.Transparent else KalaBorder),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { onLanguageSelected(lang) }
                    .padding(horizontal = if (isCompact) 10.dp else 16.dp)
                    .testTag("lang_${lang.code}")
            ) {
                Text(
                    text = lang.nativeName,
                    style = if (isCompact) Typography.labelMedium else Typography.labelLarge,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: ListingStatus,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val (borderColor, textColor) = when (status) {
        ListingStatus.SAVED -> Pair(Color(0xFF388E3C), Color(0xFF388E3C))
        ListingStatus.DRAFT -> Pair(Color(0xFFD4A017), Color(0xFFD4A017))
        ListingStatus.PENDING_UPLOAD -> Pair(KalaWarning, KalaWarning)
        ListingStatus.UPLOADING -> Pair(KalaPrimary, KalaPrimary)
        else -> Pair(KalaTextMuted, KalaTextMuted)
    }

    val label = when (status) {
        ListingStatus.FAILED, ListingStatus.PENDING_CONFIRM, ListingStatus.ARCHIVED -> status.label
        ListingStatus.SAVED -> KalaSetuStrings.savedStatus(language)
        ListingStatus.DRAFT -> when (language) {
            AppLanguage.TELUGU -> "చిత్తుప్రతి"
            AppLanguage.HINDI -> "ड्राफ्ट"
            AppLanguage.ENGLISH -> "Draft"
        }
        ListingStatus.PENDING_UPLOAD -> KalaSetuStrings.pendingUpload(language)
        ListingStatus.UPLOADING -> when (language) {
            AppLanguage.TELUGU -> "అప్‌లోడ్ అవుతోంది"
            AppLanguage.HINDI -> "अपलोड हो रहा है"
            AppLanguage.ENGLISH -> "Uploading"
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.ENGLISH,
    onDeleteClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFF8F4F0)),
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
                        painter = painterResource(id = R.drawable.ic_terracotta_pot_detailed),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Info Zone
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = KalaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = KalaSetuStrings.localizedCategory(product.category.displayName, language),
                    style = Typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Color(0xFF8A7A70),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹ ${product.finalPrice.toInt()}",
                        style = Typography.headlineSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KalaText
                        )
                    )
                    StatusChip(status = product.status, language = language)
                }
            }
        }
    }
}

@Composable
fun ProgressStepRow(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    isLast: Boolean = false,
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.ENGLISH
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Icon Circle (28dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF388E3C)
                            isActive -> KalaPrimary
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            when {
                                isCompleted -> Color(0xFF388E3C)
                                isActive -> KalaPrimary
                                else -> Color(0xFFD0C5BD)
                            }
                        ),
                        CircleShape
                    )
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isActive) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "In progress",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isCompleted -> when (language) {
                            AppLanguage.TELUGU -> "పూర్తయింది"
                            AppLanguage.HINDI -> "पूर्ण हुआ"
                            AppLanguage.ENGLISH -> "COMPLETED"
                        }
                        isActive -> when (language) {
                            AppLanguage.TELUGU -> "ప్రోగ్రెస్‌లో ఉంది"
                            AppLanguage.HINDI -> "जारी है"
                            AppLanguage.ENGLISH -> "IN PROGRESS"
                        }
                        else -> when (language) {
                            AppLanguage.TELUGU -> "వేచి ఉంది"
                            AppLanguage.HINDI -> "प्रतीक्षारत"
                            AppLanguage.ENGLISH -> "PENDING"
                        }
                    },
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8A7A70),
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = Typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted || isActive) KalaText else Color(0xFF8A7A70)
                    )
                )
            }

            // Right Dots Indicator for In Progress
            if (isActive) {
                Text(
                    text = "•••",
                    style = Typography.titleMedium.copy(
                        color = KalaPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF0EBE5))
            )
        }
    }
}

@Composable
fun TagChip(
    text: String,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFFF6ECE3))
            .border(BorderStroke(1.dp, Color(0xFFE5D5C8)), RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = Typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = KalaText
            )
        )
    }
}

@Composable
fun BottomActionBar(
    buttonText: String,
    onButtonClick: () -> Unit,
    enabled: Boolean = true,
    footnote: String? = null,
    modifier: Modifier = Modifier,
    testTag: String = "bottom_action_bar_button"
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = buttonText,
                onClick = onButtonClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                testTag = testTag
            )

            if (footnote != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = footnote,
                    style = Typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF6D5E57)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VoiceRecorderButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_rings")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(210.dp)
            .testTag("voice_record_button")
    ) {
        // Concentric glowing rings matching screenshot 1
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(if (isRecording) ringPulse else 1f)
                .clip(CircleShape)
                .background(KalaPrimary.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(if (isRecording) ringPulse * 0.98f else 1f)
                .clip(CircleShape)
                .background(KalaPrimary.copy(alpha = 0.16f))
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(KalaPrimary.copy(alpha = 0.28f))
        )

        // Core Terracotta Button (104dp)
        Surface(
            onClick = onClick,
            modifier = Modifier.size(104.dp),
            shape = CircleShape,
            color = KalaPrimary,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    // Symmetrical heights matching screenshot 1
    val baseHeights = listOf(
        6f, 10f, 14f, 20f, 32f, 42f, 38f, 22f, 26f, 34f, 40f, 36f, 26f, 44f, 36f, 26f, 36f, 40f, 34f, 26f, 22f, 38f, 42f, 32f, 20f, 14f, 10f, 6f
    )

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        val barCount = baseHeights.size
        val totalSpacing = size.width / barCount
        val barWidth = 4.5.dp.toPx()

        for (i in 0 until barCount) {
            val x = i * totalSpacing + totalSpacing / 2
            val h = if (isRecording) {
                val animatedFactor = 0.7f + 0.3f * sin(phase + i * 0.5f)
                baseHeights[i].dp.toPx() * animatedFactor
            } else {
                baseHeights[i].dp.toPx()
            }

            val top = (size.height - h) / 2
            val bottom = top + h

            drawLine(
                color = KalaPrimary,
                start = androidx.compose.ui.geometry.Offset(x, top),
                end = androidx.compose.ui.geometry.Offset(x, bottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
