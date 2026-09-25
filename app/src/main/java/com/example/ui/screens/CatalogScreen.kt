package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.ui.components.ProductCard
import com.example.ui.theme.KalaPrimary
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.KalaSetuViewModel

@Composable
fun CatalogScreen(
    viewModel: KalaSetuViewModel,
    onNavigateToCapture: () -> Unit,
    onSelectProduct: (Product) -> Unit = {},
    initialStatus: ListingStatus? = null
) {
    val products by viewModel.products.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var statusName by rememberSaveable(initialStatus) { mutableStateOf(initialStatus?.name) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var sortName by rememberSaveable { mutableStateOf(CatalogSort.NEWEST.name) }
    var compactList by rememberSaveable { mutableStateOf(false) }
    val status = statusName?.let { ListingStatus.valueOf(it) }
    val category = categoryName?.let { CraftCategory.valueOf(it) }
    val filtered = remember(products, query, statusName, categoryName, sortName) {
        filterCatalog(products, query, status, category, CatalogSort.valueOf(sortName))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize().testTag("catalog_screen")
    ) {
        // Section Header
        item(span = { GridItemSpan(2) }) {
            SectionTitle(
                if (initialStatus == ListingStatus.DRAFT) "My Catalog · Drafts" else "My Catalog",
                "Your craft, thoughtfully collected."
            )
        }

        // Modern Pill Search Bar
        item(span = { GridItemSpan(2) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(label("Search products, categories, tags"), style = Typography.bodyMedium) },
                label = { Text(label("Search products, categories, tags")) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = KalaPrimary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, label("Clear search"))
                        }
                    }
                },
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KalaPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search")
            )
        }

        // Status Filter Chips
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = status == null,
                    onClick = { statusName = null },
                    label = { Text(label("All"), fontWeight = if (status == null) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(100.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KalaPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                ListingStatus.entries.forEach { value ->
                    val isSelected = status == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { statusName = value.name },
                        label = { Text(label(value.label), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KalaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Category & Sort Menus
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChoiceMenu(
                    selected = category?.displayName ?: "All categories",
                    options = listOf("All categories") + CraftCategory.entries.map { it.displayName }
                ) {
                    categoryName = CraftCategory.entries.find { value -> value.displayName == it }?.name
                }

                val sortNames = listOf("Newest", "Oldest", "Price: low to high", "Price: high to low", "Recently updated")
                ChoiceMenu(
                    selected = sortNames[CatalogSort.valueOf(sortName).ordinal],
                    options = sortNames
                ) {
                    sortName = CatalogSort.entries[sortNames.indexOf(it)].name
                }
            }
        }

        // Action Toolbar
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = viewModel::refreshCatalog) {
                        Text(label("Refresh"), color = KalaPrimary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onNavigateToCapture) {
                        Text(label("Create listing"), color = KalaPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filtered.isNotEmpty() && (query.isNotBlank() || status != null || category != null)) {
                        TextButton(onClick = { query = ""; statusName = null; categoryName = null }) {
                            Text(label("Clear filters"), color = MaterialTheme.colorScheme.error)
                        }
                    }

                    IconToggleButton(
                        checked = compactList,
                        onCheckedChange = { compactList = it }
                    ) {
                        Icon(
                            imageVector = if (compactList) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                            contentDescription = label(if (compactList) "Grid view" else "List view"),
                            tint = KalaPrimary
                        )
                    }
                }
            }
        }

        // Product Count Subheader
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "${filtered.size} ${label("products")}",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Empty states
        if (products.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                EmptyProducts(onNavigateToCapture)
            }
        } else if (filtered.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionTitle("No matching products", "Try another search or change your filters.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { query = ""; statusName = null; categoryName = null },
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(label("Reset filters"))
                        }
                    }
                }
            }
        }

        // Product Items (Grid or Compact List)
        items(
            filtered,
            key = { it.id },
            span = { GridItemSpan(if (compactList) 2 else 1) }
        ) { product ->
            if (compactList) {
                ProductRow(product) { onSelectProduct(product) }
            } else {
                ProductCard(
                    product = product,
                    onClick = { onSelectProduct(product) },
                    language = LocalAppLanguage.current
                )
            }
        }
    }
}

@Composable
fun ChoiceMenu(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label(selected),
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), style = Typography.bodyMedium) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}
