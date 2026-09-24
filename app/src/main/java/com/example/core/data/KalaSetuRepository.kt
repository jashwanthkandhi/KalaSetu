package com.example.core.data

import com.example.R
import com.example.core.model.CraftCategory
import com.example.core.model.ListingStatus
import com.example.core.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class KalaSetuRepository(
    private val productDao: ProductDao,
    private val offlineQueueDao: OfflineQueueDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomain() }
    }

    val offlineQueue: Flow<List<OfflineQueueEntity>> = offlineQueueDao.getAllQueueItems()

    suspend fun ensureInitialData() {
        if (productDao.getProductCount() == 0) {
            val initialListings = listOf(
                Product(
                    id = UUID.randomUUID().toString(),
                    title = "Terracotta Water Pot",
                    description = "Traditional handmade terracotta pot designed for everyday use. Natural clay, eco-friendly.",
                    category = CraftCategory.POTTERY,
                    tags = listOf("terracotta", "handmade", "kitchen"),
                    sampleDrawableRes = R.drawable.ic_terracotta_pot_detailed,
                    suggestedPrice = 450.0,
                    finalPrice = 450.0,
                    status = ListingStatus.SAVED,
                    createdAt = System.currentTimeMillis() - 86400000 * 2
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    title = "Bamboo Storage Basket",
                    description = "Eco-friendly handwoven bamboo cane storage basket for home organization and decor.",
                    category = CraftCategory.HOME_DECOR,
                    tags = listOf("bamboo", "storage", "handmade", "eco-friendly"),
                    sampleDrawableRes = R.drawable.ic_bamboo_basket_detailed,
                    suggestedPrice = 799.0,
                    finalPrice = 799.0,
                    status = ListingStatus.SAVED,
                    createdAt = System.currentTimeMillis() - 86400000
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    title = "Block Print Dupatta",
                    description = "Hand block-printed pure cotton dupatta with authentic floral motifs and organic dyes.",
                    category = CraftCategory.TEXTILES,
                    tags = listOf("cotton", "handblock", "dupatta", "textiles"),
                    sampleDrawableRes = R.drawable.ic_block_print_detailed,
                    suggestedPrice = 1200.0,
                    finalPrice = 1200.0,
                    status = ListingStatus.DRAFT,
                    createdAt = System.currentTimeMillis() - 3600000 * 12
                )
            )
            productDao.insertProducts(initialListings.map { ProductEntity.fromDomain(it) })
        }
    }

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(ProductEntity.fromDomain(product))
    }

    suspend fun deleteProduct(id: String) {
        productDao.deleteProductById(id)
    }

    suspend fun enqueueOfflineItem(item: OfflineQueueEntity) {
        offlineQueueDao.enqueueItem(item)
    }

    suspend fun removeOfflineItem(id: String) {
        offlineQueueDao.deleteItem(id)
    }
}
