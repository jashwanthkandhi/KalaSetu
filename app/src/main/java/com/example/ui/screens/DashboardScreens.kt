package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaBorder
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.KalaSurface
import com.example.ui.theme.KalaText
import com.example.ui.theme.KalaTextMuted
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable fun HomeScreen(vm: KalaSetuViewModel, navigate: (String) -> Unit, select: (Product) -> Unit) {
    val products by vm.products.collectAsState()
    val profile by vm.profile.collectAsState()
    val online by vm.isOnline.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val stats = catalogStats(products)
    val greeting = when(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) { in 0..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Top Brand & Greeting Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KalaPrimary,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("KS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("KALASETU", style = Typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = KalaPrimary))
                            }
                            Text(label("Digital Craft Bridge"), style = Typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
                        }
                    }

                    // Online / Offline Status Beacon Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (online) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, if (online) Color(0xFFA5D6A7) else Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (online) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Text(
                                text = label(if (online) "Online" else "Offline"),
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (online) Color(0xFF15803D) else Color(0xFFB45309),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${label(greeting)}, ${profile.display_name.ifBlank { label("Artisan") }}",
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = label("Make room for your next creation."),
                        style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Hero Creation Banner
        item {
            CreationHero { navigate("capture") }
        }

        // Metric Statistics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = label("Catalog overview"),
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val metricItems = listOf(
                        Triple("Total products", stats.total, Triple(Icons.Outlined.Inventory2, KalaPrimary, Color(0xFFFFF7ED))),
                        Triple("Published", stats.published, Triple(Icons.Outlined.CheckCircle, Color(0xFF16A34A), Color(0xFFF0FDF4))),
                        Triple("Drafts", stats.drafts, Triple(Icons.Outlined.EditNote, Color(0xFFD97706), Color(0xFFFEF9C3))),
                        Triple("Pending sync", stats.pending, Triple(Icons.Outlined.CloudUpload, Color(0xFF4F46E5), Color(0xFFEEF2FF))),
                        Triple("Failed", stats.failed, Triple(Icons.Outlined.ErrorOutline, Color(0xFFDC2626), Color(0xFFFEF2F2)))
                    )

                    metricItems.forEach { (name, count, styling) ->
                        val (icon, accentColor, tintBg) = styling
                        Surface(
                            onClick = {
                                navigate(when(name) {
                                    "Drafts" -> "drafts"
                                    "Pending sync", "Failed" -> "sync"
                                    else -> "catalog"
                                })
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 140.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(tintBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                    }

                                    Text(
                                        text = count.toString(),
                                        style = Typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp,
                                            color = if (count > 0 && name == "Failed") Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Text(
                                    text = label(name),
                                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Continue Creation Draft Card if any draft exists
        products.firstOrNull { it.status == ListingStatus.DRAFT && it.sampleDrawableRes == null }?.let { draft ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Continue your creation", "Your work is right where you left it.")
                    ProductRow(draft) { select(draft) }
                }
            }
        }

        // Quick Navigation Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { navigate("drafts") },
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = KalaPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text(label("Drafts"), style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }
                }

                Surface(
                    onClick = { navigate("discover") },
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(18.dp), tint = KalaPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text(label("Discover"), style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }
                }

                Surface(
                    onClick = { navigate("catalog") },
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(18.dp), tint = KalaPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text(label("Catalog"), style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }
                }
            }
        }

        // Live Sync Center Card
        item {
            Surface(
                onClick = { navigate("sync") },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (online) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (online) Icons.Outlined.CloudDone else Icons.Outlined.Sync,
                            contentDescription = null,
                            tint = if (online) Color(0xFF16A34A) else Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(label(if (online) "Online" else "Offline"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(
                            if (stats.pending + stats.failed == 0) label("No work waiting for sync")
                            else "${stats.pending} pending · ${stats.failed} ${label("need attention")}",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label("Sync center"), color = MaterialTheme.colorScheme.primary, style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Tips & Guidance Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = KalaBanner,
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFC2410C),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (stats.drafts > 0) "${label("You have ")}${stats.drafts}${label(" drafts to review. Your products publish only after you confirm.")}"
                        else label("Photo tip: use daylight and a simple background to show the texture of your craft."),
                        style = Typography.bodyMedium.copy(
                            color = Color(0xFF9A3412),
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Recent Products
        if (!prefs.simpleView) {
            val recents = products.filter { it.sampleDrawableRes == null }.take(3)
            if (recents.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Recent products")
                        TextButton(onClick = { navigate("catalog") }) {
                            Text(label("View all"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(recents, key = { it.id }) { product ->
                    ProductRow(product) { select(product) }
                }
            }
        }

        if (products.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = label("Your collection starts with one creation."),
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable fun InsightsScreen(vm: KalaSetuViewModel) {
    val allProducts by vm.products.collectAsState()
    val products = allProducts.filter { it.sampleDrawableRes == null }
    val stats = catalogStats(products)

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { SectionTitle("Insights", "A clear view of your work on this device.") }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = KalaBanner,
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${stats.thisWeek} ${label("products created in the last 7 days")}",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                    )
                    Text(
                        label("Consistent cataloging helps build your digital craft identity."),
                        style = Typography.bodyMedium.copy(color = Color(0xFFC2410C))
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(8.dp)) {
                    listOf(
                        "Total products" to stats.total,
                        "Published" to stats.published,
                        "Drafts" to stats.drafts,
                        "Pending sync" to stats.pending,
                        "Failed" to stats.failed
                    ).forEachIndexed { idx, (name, count) ->
                        ListItem(
                            headlineContent = {
                                Text(label(name), style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                            },
                            trailingContent = {
                                Text(count.toString(), style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            }
                        )
                        if (idx < 4) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)
                        }
                    }
                }
            }
        }

        item { SectionTitle("Categories used") }
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val grouped = products.filter { it.title.isNotBlank() }.groupingBy { it.category }.eachCount().toList()
                    if (grouped.isEmpty()) {
                        Text(label("No categories recorded yet."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        grouped.forEach { (category, count) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(category.displayName, style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text("$count", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = KalaPrimary))
                                }
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / products.size.coerceAtLeast(1) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(100.dp)),
                                    color = KalaPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SectionTitle("Languages used") }
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val langGrouped = products.groupingBy { it.languageCode }.eachCount().toList()
                    if (langGrouped.isEmpty()) {
                        Text(label("No voice language records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        langGrouped.forEach { (lang, count) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(AppLanguage.entries.find { it.code == lang }?.nativeName ?: lang, style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                                Text("$count", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                label("Buyer views, orders and sales are not tracked. These insights use your real catalog records only."),
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable fun SyncScreen(vm: KalaSetuViewModel, select: (Product) -> Unit) {
    val products by vm.products.collectAsState()
    val queue by vm.offlineQueue.collectAsState()
    val online by vm.isOnline.collectAsState()
    val relevant = products.filter { it.status in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_UPLOAD, ListingStatus.PENDING_CONFIRM, ListingStatus.FAILED, ListingStatus.DRAFT) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Sync center", if (online) "Online" else "Offline") }
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        label("Photos and voice notes stay on this device while processing is pending. Generated listings wait for your review."),
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item { ActionButton("Retry all", vm::syncOfflineQueue) }
        if (relevant.isEmpty()) item {
            Text(label("No work waiting for sync."), style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(relevant, key = { it.id }) { product ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProductRow(product) { select(product) }
                    product.lastError?.let {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(it, modifier = Modifier.padding(10.dp), color = MaterialTheme.colorScheme.error, style = Typography.bodySmall)
                        }
                    }
                    queue.find { it.id == product.id }?.let { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${label("Attempts: ")}${item.retryCount} / 3", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { vm.retryProduct(product) },
                                enabled = product.status != ListingStatus.UPLOADING,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KalaPrimary)
                            ) {
                                Text(label("Retry"))
                            }
                        }
                    }
                }
            }
        }
    }
}
