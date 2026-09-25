package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.model.*
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
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("K A L A S E T U", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            SectionTitle("${label(greeting)}, ${profile.display_name.ifBlank { label("Artisan") }}", "Make room for your next creation.")
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(label("Your craft deserves a bigger world."), style = MaterialTheme.typography.headlineSmall)
                    Text(label("Photograph it. Tell its story. Review and share."))
                    ActionButton("Create listing", { navigate("capture") })
                }
            }
        }
        item { FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Total products" to stats.total, "Published" to stats.published, "Drafts" to stats.drafts,
                "Pending sync" to stats.pending, "Failed" to stats.failed).forEach { (name, count) ->
                OutlinedCard(Modifier.widthIn(min = 135.dp)) {
                    Column(Modifier.padding(16.dp)) { Text(count.toString(), style = MaterialTheme.typography.headlineLarge); Text(label(name)) }
                }
            }
        } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ navigate("drafts") }) { Text(label("Drafts")) }
                OutlinedButton({ navigate("discover") }) { Text(label("Discover")) }
                OutlinedButton({ navigate("catalog") }) { Text(label("Catalog")) }
            }
        }
        item {
            OutlinedCard(onClick = { navigate("sync") }) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(label(if (online) "Online" else "Offline"), style = MaterialTheme.typography.titleMedium)
                    Text(if (stats.pending + stats.failed == 0) label("No work waiting for sync") else "${stats.pending} pending · ${stats.failed} ${label("need attention")}")
                    Text(label("Sync center"), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text(if (stats.drafts > 0) "${label("You have ")}${stats.drafts}${label(" drafts to review. Your products publish only after you confirm.")}"
                else label("Photo tip: use daylight and a simple background to show the texture of your craft."))
        }
        if (!prefs.simpleView) {
            item { SectionTitle("Recent products") }
            items(products.filter { it.sampleDrawableRes == null }.take(3), key = { it.id }) { ProductRow(it) { select(it) } }
        }
        if (products.isEmpty()) item { Text(label("Your collection starts with one creation.")) }
    }
}

@Composable fun InsightsScreen(vm: KalaSetuViewModel) {
    val allProducts by vm.products.collectAsState()
    val products = allProducts.filter { it.sampleDrawableRes == null }
    val stats = catalogStats(products)
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Insights", "A clear view of your work on this device.") }
        item { Text("${stats.thisWeek} ${label("products created in the last 7 days")}", style = MaterialTheme.typography.headlineSmall) }
        items(listOf("Total products" to stats.total, "Published" to stats.published, "Drafts" to stats.drafts,
            "Pending sync" to stats.pending, "Failed" to stats.failed)) { (name, count) ->
            ListItem(headlineContent = { Text(label(name)) }, trailingContent = { Text(count.toString(), style = MaterialTheme.typography.titleLarge) })
        }
        item { SectionTitle("Categories used") }
        items(products.filter { it.title.isNotBlank() }.groupingBy { it.category }.eachCount().toList()) { (category, count) ->
            Text("${category.displayName} · $count")
            LinearProgressIndicator(progress = { count.toFloat() / products.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth())
        }
        item { SectionTitle("Languages used") }
        items(products.groupingBy { it.languageCode }.eachCount().toList()) { (lang, count) ->
            Text("${AppLanguage.entries.find { it.code == lang }?.nativeName ?: lang} · $count")
        }
        item { Text(label("Buyer views, orders and sales are not tracked. These insights use your real catalog records only."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable fun SyncScreen(vm: KalaSetuViewModel, select: (Product) -> Unit) {
    val products by vm.products.collectAsState()
    val queue by vm.offlineQueue.collectAsState()
    val online by vm.isOnline.collectAsState()
    val relevant = products.filter { it.status in listOf(ListingStatus.UPLOADING, ListingStatus.PENDING_UPLOAD, ListingStatus.PENDING_CONFIRM, ListingStatus.FAILED, ListingStatus.DRAFT) }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Sync center", if (online) "Online" else "Offline") }
        item { Text(label("Photos and voice notes stay on this device while processing is pending. Generated listings wait for your review.")) }
        item { ActionButton("Retry all", vm::syncOfflineQueue) }
        if (relevant.isEmpty()) item { Text(label("No work waiting for sync.")) }
        items(relevant, key = { it.id }) { product ->
            ProductRow(product) { select(product) }
            product.lastError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            queue.find { it.id == product.id }?.let { item ->
                Text("${label("Attempts: ")}${item.retryCount} / 3")
                TextButton({ vm.retryProduct(product) }, enabled = product.status != ListingStatus.UPLOADING) { Text(label("Retry")) }
            }
        }
    }
}


