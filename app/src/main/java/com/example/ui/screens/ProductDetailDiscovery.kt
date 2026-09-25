package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.core.model.*
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
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ProductImage(product, original) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(original, { original = true }, label = { Text(label("Original")) })
                if (!product.imageWarning && product.imageUrl != product.originalImageUrl)
                    FilterChip(!original, { original = false }, label = { Text(label("Enhanced")) })
            }
            if (product.imageWarning) Text(label("Enhancement unavailable. Your original photo is preserved."))
        }
        item { SectionTitle(displayTitle, label(product.category.displayName)); StatusLabel(product) }
        item { Text(if (product.finalPrice > 0) money(product.finalPrice) else label("Price not set"), style = MaterialTheme.typography.headlineLarge) }
        item { Text(product.description.ifBlank { label("Complete capture to generate a listing.") }) }
        if (product.tags.isNotEmpty()) item { Text(product.tags.joinToString(" · "), color = MaterialTheme.colorScheme.primary) }
        items(product.attributes.toList()) { (name, value) -> Text("$name: $value") }
        if (owned) item { PriceGuidance(product) }
        item { Text("${label("Created")} ${dateLabel(product.createdAt)} · ${label("Updated")} ${dateLabel(product.updatedAt)}", style = MaterialTheme.typography.bodySmall) }
        item {
            val p = product.publicProfile
            if (p.display_name.isNotBlank() || p.shop_name.isNotBlank()) {
                SectionTitle(p.shop_name.ifBlank { p.display_name }, "${p.display_name} · ${p.craft}")
                Text(p.bio); Text(p.location)
            }
        }
        if (product.publicProfile.contact_public && product.publicProfile.contact.isNotBlank()) item {
            ActionButton("Contact artisan", {
                val phone = product.publicProfile.contact.filter { it.isDigit() || it == '+' }
                runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))) }
                    .onFailure { Toast.makeText(context, translated("No phone app is available.", language), Toast.LENGTH_SHORT).show() }
            })
        }
        item { ActionButton("Share", { shareProduct(context, product) }, product.title.isNotBlank()) }
        if (!owned) item { OutlinedButton({ vm.toggleFavorite(product.id) }) { Text(label(if (product.id in favorites) "Remove favorite" else "Save favorite")) } }
        if (owned) {
            item { ActionButton("Edit", { edit(product) }, !busy && product.status !in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_CONFIRM)) }
            item { OutlinedButton({ vm.duplicateProduct(product) { vm.currentDraft.value?.let(edit) } }, enabled = !busy && product.title.isNotBlank()) { Text(label("Duplicate")) } }
            if (product.status == ListingStatus.FAILED) item { ActionButton("Retry", { vm.retryProduct(product) }, !busy) }
            if (product.status == ListingStatus.SAVED) item { OutlinedButton({ vm.archiveProduct(product) }, enabled = !busy) { Text(label("Archive")) } }
            item { TextButton({ deleteDialog = true }, enabled = !busy && product.status !in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_CONFIRM)) { Text(label("Delete"), color = MaterialTheme.colorScheme.error) } }
        }
    }
    if (deleteDialog) AlertDialog(onDismissRequest = { deleteDialog = false }, title = { Text(label("Delete this product?")) },
        text = { Text(label("This removes the listing and its pending work. Published listings are removed from discovery only after the cloud confirms deletion. This cannot be undone.")) },
        confirmButton = { TextButton({ deleteDialog = false; vm.deleteProduct(product.id, deleted) }) { Text(label("Delete")) } },
        dismissButton = { TextButton({ deleteDialog = false }) { Text(label("Cancel")) } })
}
@Composable fun PriceGuidance(product: Product) {
    OutlinedCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Price guidance")
            Text("${label("Suggested: ")}${money(product.suggestedPrice)}", style = MaterialTheme.typography.titleLarge)
            val market = product.marketData
            if (market.low != null && market.high != null) Text("${label("Market range: ")}${money(market.low)} – ${money(market.high)}")
            Text(market.explanation)
            market.comparables.take(5).forEach { Text("${it.title} · ${money(it.price)} · ${it.source}", style = MaterialTheme.typography.bodySmall) }
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
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Discover", "Handcrafted products. Direct artisan connections.") }
        item { OutlinedTextField(query, { query = it }, label = { Text(label("Search products, categories, tags")) }, modifier = Modifier.fillMaxWidth()) }
        item { ChoiceMenu(category?.displayName ?: "All categories", listOf("All categories") + CraftCategory.entries.map { it.displayName }) {
            category = CraftCategory.entries.find { c -> c.displayName == it }
        } }
        item { FilterChip(savedOnly, { savedOnly = !savedOnly }, label = { Text(label("Favorites")) }) }
        item { TextButton({ vm.loadDiscovery() }, enabled = !loading) { Text(label("Refresh")) } }
        if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        error?.let { item { Text(it, color = MaterialTheme.colorScheme.error); ActionButton("Retry", { vm.loadDiscovery() }) } }
        if (!loading && error == null && filtered.isEmpty()) item { SectionTitle("No matching products", "Published artisan products will appear here. Try another search or return later.") }
        items(filtered, key = { it.id }) { ProductRow(it) { select(it) } }
        if (more && products.isNotEmpty()) item { ActionButton("Load more", { vm.loadDiscovery(true) }, !loading) }
    }
}

