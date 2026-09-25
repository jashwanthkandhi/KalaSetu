package com.example.core.data

import androidx.room.withTransaction
import com.example.core.model.Product
import kotlinx.coroutines.flow.map

class KalaSetuRepository(private val db: KalaSetuDatabase) {
    private val productDao = db.productDao()
    private val queueDao = db.offlineQueueDao()
    val allProducts = productDao.getAllProducts().map { rows -> rows.map { it.toDomain() } }
    val offlineQueue = queueDao.getAllQueueItems()
    suspend fun saveProduct(product: Product) = productDao.insertProduct(ProductEntity.fromDomain(product))
    suspend fun getProduct(id: String) = productDao.getProductById(id)?.toDomain()
    suspend fun queueItems() = queueDao.snapshot()
    suspend fun allSnapshot() = productDao.snapshot().map { it.toDomain() }
    suspend fun enqueue(product: Product, item: OfflineQueueEntity) = db.withTransaction {
        saveProduct(product)
        queueDao.enqueueItem(item)
    }
    suspend fun completeProcessing(product: Product) = db.withTransaction {
        saveProduct(product)
        queueDao.deleteItem(product.id)
    }
    suspend fun deleteProduct(id: String) = db.withTransaction {
        queueDao.deleteItem(id)
        productDao.deleteProductById(id)
    }
    suspend fun enqueueOfflineItem(item: OfflineQueueEntity) = queueDao.enqueueItem(item)
    suspend fun recoverInterrupted() = db.withTransaction {
        queueDao.recoverInterrupted()
        queueDao.snapshot().forEach { item ->
            val product = getProduct(item.id)
            if (product?.status == com.example.core.model.ListingStatus.UPLOADING) {
                saveProduct(product.copy(status = com.example.core.model.ListingStatus.PENDING_UPLOAD,
                    lastError = "Processing was interrupted. Your media is safe and waiting to retry."))
            }
        }
    }
}
