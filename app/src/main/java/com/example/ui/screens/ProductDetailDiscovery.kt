package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.ui.components.ProductCard
import com.example.ui.components.StatusChip
import com.example.ui.components.TagChip
import com.example.ui.theme.KalaBanner
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

fun shareProduct(context: Context, product: Product) {
    val profile = product.publicProfile
    val text = buildString {
        append("${product.title}\n${product.description}\n${money(product.finalPrice)}\n${product.category.displayName}")
        if (profile.shop_name.isNotBlank()) append("\n${profile.shop_name}")
        if (profile.display_name.isNotBlank()) append("\n${profile.display_name}")
        if (profile.contact_public && profile.contact.isNotBlank()) append("\nContact: ${profile.contact}")
    }
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share product"))
}

@Composable fun DetailScreen(product: Product, vm: KalaSetuViewModel, owned: Boolean, edit: (Product) -> Unit, deleted: () -> Unit) {
    var original by rememberSaveable { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val language by vm.currentLanguage.collectAsState()
    val displayTitle = product.title.ifBlank { translated("Drafts", language) }
    val favorites by vm.favorites.collectAsState()
    val busy by vm.busy.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Hero Image Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ProductImage(product, original)
                        StatusChip(
                            status = product.status,
                            language = language,
                            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = original,
                                onClick = { original = true },
                                label = { Text(label("Original"), fontWeight = if (original) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalaPrimary, selectedLabelColor = Color.White)
                            )
                            if (!product.imageWarning && product.imageUrl != product.originalImageUrl) {
                                FilterChip(
                                    selected = !original,
                                    onClick = { original = false },
                                    label = { Text(label("Enhanced"), fontWeight = if (!original) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(100.dp),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalaPrimary, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }

                    if (product.imageWarning) {
                        Text(
                            text = label("Enhancement unavailable. Your original photo is preserved."),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Title & Price Section
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = displayTitle,
                                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = label(product.category.displayName),
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = KalaPrimary)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (product.finalPrice > 0) money(product.finalPrice) else label("Price not set"),
                            style = Typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = KalaPrimary,
                                fontSize = 28.sp
                            )
                        )
                        StatusLabel(product)
                    }
                }
            }
        }

        // Story & Description Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(label("Description"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = product.description.ifBlank { label("Complete capture to generate a listing.") },
                        style = Typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (product.tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            product.tags.forEach { tag ->
                                TagChip(tag)
                            }
                        }
                    }
                }
            }
        }

        // Attributes Table
        if (product.attributes.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(label("Craft details"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        product.attributes.forEach { (name, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(value, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }
        }

        // Price Guidance
        if (owned) {
            item { PriceGuidance(product) }
        }

        // Dates & Meta
        item {
            Text(
                text = "${label("Created")} ${dateLabel(product.createdAt)} · ${label("Updated")} ${dateLabel(product.updatedAt)}",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Artisan Public Profile Card
        val p = product.publicProfile
        if (p.display_name.isNotBlank() || p.shop_name.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle(p.shop_name.ifBlank { p.display_name }, "${p.display_name} · ${p.craft}")
                        if (p.bio.isNotBlank()) Text(p.bio, style = Typography.bodyMedium)
                        if (p.location.isNotBlank()) Text("📍 ${p.location}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Contact Button if public
        if (product.publicProfile.contact_public && product.publicProfile.contact.isNotBlank()) {
            item {
                Button(
                    onClick = {
                        val phone = product.publicProfile.contact.filter { it.isDigit() || it == '+' }
                        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))) }
                            .onFailure { Toast.makeText(context, translated("No phone app is available.", language), Toast.LENGTH_SHORT).show() }
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label("Contact artisan"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Share Action
        item {
            OutlinedButton(
                onClick = { shareProduct(context, product) },
                enabled = product.title.isNotBlank(),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = KalaPrimary)
                Spacer(Modifier.width(8.dp))
                Text(label("Share"), fontWeight = FontWeight.Bold, color = KalaPrimary)
            }
        }

        if (!owned) {
            item {
                OutlinedButton(
                    onClick = { vm.toggleFavorite(product.id) },
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(if (product.id in favorites) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(label(if (product.id in favorites) "Remove favorite" else "Save favorite"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Owner Actions
        if (owned) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(
                        text = "Edit",
                        onClick = { edit(product) },
                        enabled = !busy && product.status !in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_CONFIRM)
                    )

                    OutlinedButton(
                        onClick = { vm.duplicateProduct(product) { vm.currentDraft.value?.let(edit) } },
                        enabled = !busy && product.title.isNotBlank(),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(label("Duplicate"), fontWeight = FontWeight.Bold)
                    }

                    if (product.status == ListingStatus.FAILED) {
                        ActionButton("Retry", { vm.retryProduct(product) }, !busy)
                    }

                    if (product.status == ListingStatus.SAVED) {
                        OutlinedButton(
                            onClick = { vm.archiveProduct(product) },
                            enabled = !busy,
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(label("Archive"), fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { deleteDialog = true },
                        enabled = !busy && product.status !in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_CONFIRM),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label("Delete"), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text(label("Delete this product?"), style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = { Text(label("This removes the listing and its pending work. Published listings are removed from discovery only after the cloud confirms deletion. This cannot be undone.")) },
            confirmButton = {
                Button(
                    onClick = { deleteDialog = false; vm.deleteProduct(product.id, deleted) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(label("Delete"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) {
                    Text(label("Cancel"))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable fun PriceGuidance(product: Product) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = KalaPrimary)
                Text(label("Price guidance"), style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KalaBanner,
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (product.suggestedPrice > 0) "${label("Suggested: ")}${money(product.suggestedPrice)}" else label("Enter your costs to calculate a price."),
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                    )
                    val market = product.marketData
                    if (market.low != null && market.high != null) {
                        Text(
                            text = "${label("Market range: ")}${money(market.low)} – ${money(market.high)}",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFFC2410C))
                        )
                    }
                }
            }

            val market = product.marketData
            if (market.explanation.isNotBlank()) {
                Text(market.explanation, style = Typography.bodyMedium)
            }

            if (market.comparables.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    market.comparables.take(5).forEach {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.title, style = Typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("${money(it.price)} · ${it.source}", style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable fun DiscoveryScreen(vm: KalaSetuViewModel, select: (Product) -> Unit) {
    val products by vm.discovery.collectAsState()
    val loading by vm.discoveryLoading.collectAsState()
    val error by vm.discoveryError.collectAsState()
    val more by vm.discoveryHasMore.collectAsState()
    val favorites by vm.favorites.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    var category by remember { mutableStateOf<CraftCategory?>(null) }
    LaunchedEffect(Unit) { if (products.isEmpty()) vm.loadDiscovery() }
    val filtered = filterCatalog(products.filter { !savedOnly || it.id in favorites }, query, category = category)

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Discover", "Handcrafted products. Direct artisan connections.") }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(label("Search products, categories, tags")) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = KalaPrimary) },
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ChoiceMenu(category?.displayName ?: "All categories", listOf("All categories") + CraftCategory.entries.map { it.displayName }) {
                    category = CraftCategory.entries.find { c -> c.displayName == it }
                }

                FilterChip(
                    selected = savedOnly,
                    onClick = { savedOnly = !savedOnly },
                    label = { Text(label("Favorites")) },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalaPrimary, selectedLabelColor = Color.White)
                )

                TextButton(onClick = { vm.loadDiscovery() }, enabled = !loading) {
                    Text(label("Refresh"), fontWeight = FontWeight.Bold, color = KalaPrimary)
                }
            }
        }

        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(100.dp)), color = KalaPrimary) }

        error?.let {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        ActionButton("Retry", { vm.loadDiscovery() })
                    }
                }
            }
        }

        if (!loading && error == null && filtered.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("No matching products", "Published artisan products will appear here. Try another search or return later.")
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { product ->
            ProductRow(product) { select(product) }
        }

        if (more && products.isNotEmpty()) {
            item {
                ActionButton("Load more", { vm.loadDiscovery(true) }, !loading)
            }
        }
    }
}
