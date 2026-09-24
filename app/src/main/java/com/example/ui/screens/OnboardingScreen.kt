package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.KalaSetuStrings
import com.example.ui.components.LanguageSelector
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaText
import com.example.ui.theme.KalaTextMuted
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable
fun OnboardingScreen(
    viewModel: KalaSetuViewModel,
    onStartCreating: () -> Unit
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KalaBackground)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top Bar with KalaSetu Wordmark & Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kala",
                        style = Typography.headlineLarge.copy(
                            color = KalaPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Setu",
                        style = Typography.headlineLarge.copy(
                            color = KalaText,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                Text(
                    text = KalaSetuStrings.skip(currentLanguage),
                    style = Typography.bodyMedium.copy(
                        color = KalaTextMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clickable {
                            viewModel.completeOnboarding()
                            onStartCreating()
                        }
                        .padding(8.dp)
                        .testTag("skip_onboarding")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Hero Craft Visual with gentle entrance animation
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(600)
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_craft_hero),
                    contentDescription = "KalaSetu Artisan Craft",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Headline
            Text(
                text = KalaSetuStrings.appTagline(currentLanguage),
                style = Typography.displayLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = KalaText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Supporting description
            Text(
                text = KalaSetuStrings.onboardingSubtitle(currentLanguage),
                style = Typography.bodyLarge.copy(
                    color = KalaTextMuted,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Language Selector Pills
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Language / భాష / भाषा",
                    style = Typography.labelMedium.copy(color = KalaTextMuted, fontSize = 12.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LanguageSelector(
                    selectedLanguage = currentLanguage,
                    onLanguageSelected = { lang ->
                        viewModel.setLanguage(lang)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Start Creating Button
            PrimaryButton(
                text = "${KalaSetuStrings.startCreating(currentLanguage)} →",
                onClick = {
                    viewModel.completeOnboarding()
                    onStartCreating()
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "start_creating_button"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
