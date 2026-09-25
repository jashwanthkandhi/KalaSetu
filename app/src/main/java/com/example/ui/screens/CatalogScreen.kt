package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.model.*
import com.example.ui.components.ProductCard
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable
fun CatalogScreen(viewModel: KalaSetuViewModel, onNavigateToCapture: () -> Unit,
    onSelectProduct: (Product) -> Unit = {}, initialStatus: ListingStatus? = null) {
    val products by viewModel.products.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var statusName by rememberSaveable(initialStatus) { mutableStateOf(initialStatus?.name) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var sortName by rememberSaveable { mutableStateOf(CatalogSort.NEWEST.name) }
    val status = statusName?.let { ListingStatus.valueOf(it) }
    val category = categoryName?.let { CraftCategory.valueOf(it) }
    val filtered = remember(products, query, statusName, categoryName, sortName) {
        filterCatalog(products, query, status, category, CatalogSort.valueOf(sortName))
    }
    LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize().testTag("catalog_screen")) {
        item(span = { GridItemSpan(2) }) { SectionTitle(if (initialStatus == ListingStatus.DRAFT) "My Catalog · Drafts" else "My Catalog", "Your craft, thoughtfully collected.") }
        item(span = { GridItemSpan(2) }) { OutlinedTextField(query, { query = it }, label = { Text(label("Search products, categories, tags")) },
            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("catalog_search")) }
        item(span = { GridItemSpan(2) }) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(status == null, { statusName = null }, label = { Text(label("All")) })
                ListingStatus.entries.forEach { value ->
                    FilterChip(status == value, { statusName = value.name }, label = { Text(label(value.label)) })
                }
            }
        }
        item(span = { GridItemSpan(2) }) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceMenu(category?.displayName ?: "All categories",
                    listOf("All categories") + CraftCategory.entries.map { it.displayName }) {
                    categoryName = CraftCategory.entries.find { value -> value.displayName == it }?.name
                }
                val names = listOf("Newest", "Oldest", "Price: low to high", "Price: high to low", "Recently updated")
                ChoiceMenu(names[CatalogSort.valueOf(sortName).ordinal], names) { sortName = CatalogSort.entries[names.indexOf(it)].name }
            }
        }
        item(span = { GridItemSpan(2) }) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = viewModel::refreshCatalog) { Text(label("Refresh")) }
            TextButton(onClick = onNavigateToCapture) { Text(label("Create listing")) }
        } }
        if (products.isEmpty()) item(span = { GridItemSpan(2) }) { EmptyProducts(onNavigateToCapture) }
        else if (filtered.isEmpty()) item(span = { GridItemSpan(2) }) { SectionTitle("No matching products", "Try another search or change your filters.") }
        items(filtered, key = { it.id }) { product -> ProductGridCard(product) { onSelectProduct(product) } }
    }
}

@Composable private fun ProductGridCard(product: Product, onClick: () -> Unit) {
    ProductCard(product, onClick, language = LocalAppLanguage.current)
}

@Composable fun ChoiceMenu(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton({ expanded = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text(label(selected)) }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(label(option)) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}
