package com.example.core.model

data class MarketComparable(val title: String = "", val price: Double = 0.0, val source: String = "")
data class MarketData(
    val source: String = "unavailable", val low: Double? = null, val high: Double? = null,
    val explanation: String = "Live market data is unavailable.",
    val comparables: List<MarketComparable> = emptyList()
)
data class ArtisanProfile(
    val display_name: String = "", val shop_name: String = "", val craft: String = "",
    val location: String = "", val bio: String = "", val contact: String = "",
    val contact_public: Boolean = false, val email: String = "", val photo: String = ""
)
data class AppPreferences(
    val theme: String = "System", val simpleView: Boolean = false, val largerText: Boolean = false,
    val highContrast: Boolean = false, val speechSpeed: Float = 1f, val ttsLanguage: String = "auto",
    val processingNotifications: Boolean = true, val syncNotifications: Boolean = true,
    val errorNotifications: Boolean = true
)
enum class CatalogSort { NEWEST, OLDEST, PRICE_LOW, PRICE_HIGH, UPDATED }

fun filterCatalog(products: List<Product>, query: String = "", status: ListingStatus? = null,
                  category: CraftCategory? = null, sort: CatalogSort = CatalogSort.NEWEST): List<Product> {
    val terms = query.trim()
    val filtered = products.filter { product ->
        (status == null || product.status == status) && (category == null || product.category == category) &&
            (terms.isBlank() || (listOf(product.title, product.category.displayName) + product.tags).any { it.contains(terms, true) })
    }
    return when (sort) {
        CatalogSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
        CatalogSort.OLDEST -> filtered.sortedBy { it.createdAt }
        CatalogSort.PRICE_LOW -> filtered.sortedBy { it.finalPrice }
        CatalogSort.PRICE_HIGH -> filtered.sortedByDescending { it.finalPrice }
        CatalogSort.UPDATED -> filtered.sortedByDescending { it.updatedAt }
    }
}

fun Product.qualityIssues(): List<String> = buildList {
    if (title.isBlank() || title.length > 80) add("Use a title of 1–80 characters.")
    if (description.isBlank() || description.length > 400) add("Use a description of 1–400 characters.")
    if (originalImageUrl.isNullOrBlank()) add("Process a product photo before publishing.")
    if (tags.isEmpty() || tags.size > 12 || tags.any { it.isBlank() || it.length > 40 }) add("Add 1–12 tags, each up to 40 characters.")
    if (!finalPrice.isFinite() || finalPrice <= 0 || finalPrice > 50000) add("Set a price above ₹0 and no more than ₹50,000.")
}

data class CatalogStats(val total: Int, val published: Int, val drafts: Int, val pending: Int, val failed: Int, val thisWeek: Int)
fun catalogStats(allProducts: List<Product>, now: Long = System.currentTimeMillis()): CatalogStats {
    val products = allProducts.filter { it.sampleDrawableRes == null }
    return CatalogStats(
    products.size, products.count { it.status == ListingStatus.SAVED }, products.count { it.status == ListingStatus.DRAFT },
    products.count { it.status in listOf(ListingStatus.PENDING_UPLOAD, ListingStatus.UPLOADING, ListingStatus.PENDING_CONFIRM) },
    products.count { it.status == ListingStatus.FAILED }, products.count { it.createdAt in (now - 7 * 86400000L)..now }
)
}
