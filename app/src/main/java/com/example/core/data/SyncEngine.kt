package com.example.core.data

import android.content.Context
import androidx.work.*
import com.example.core.model.*
import com.example.core.network.NetworkApiService
import com.example.core.service.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

class SyncEngine(private val repository: KalaSetuRepository, private val api: ApiService) {
    companion object { private val lock = Mutex() }
    suspend fun process(id: String, onStageUpdate: (ProcessingStage) -> Unit = {}): Product? = lock.withLock {
        val item = repository.queueItems().find { it.id == id } ?: return@withLock repository.getProduct(id)
        val existing = repository.getProduct(id) ?: return@withLock null
        repository.enqueueOfflineItem(item.copy(status = "uploading"))
        repository.saveProduct(existing.copy(status = ListingStatus.UPLOADING, lastError = null))
        try {
            val language = AppLanguage.entries.firstOrNull { it.code == item.languageCode } ?: AppLanguage.ENGLISH
            val response = api.processListing(item.photoUri, item.audioPath, language, onStageUpdate)
            val product = existing.copy(title = response.title, description = response.description,
                category = response.category, tags = response.tags, imageUrl = response.enhancedImageUrl,
                originalImageUrl = response.originalImageUrl, imageWarning = response.imageWarning,
                voiceTranscript = response.transcript, suggestedPrice = response.suggestedPrice,
                finalPrice = response.suggestedPrice, status = ListingStatus.DRAFT,
                marketData = response.marketData, attributes = response.attributes,
                languageCode = item.languageCode, updatedAt = System.currentTimeMillis(), lastError = null)
            repository.completeProcessing(product)
            product
        } catch (cancelled: CancellationException) {
            repository.enqueueOfflineItem(item.copy(status = "queued"))
            throw cancelled
        } catch (e: Exception) {
            repository.enqueueOfflineItem(item.copy(status = "failed", retryCount = item.retryCount + 1,
                lastError = "Processing failed. Check your connection and retry."))
            repository.saveProduct(existing.copy(status = ListingStatus.FAILED,
                lastError = "Processing failed. Your photo and recording are safe. Retry from the sync center."))
            throw e
        }
    }
    suspend fun confirm(product: Product, reviewedNow: Boolean = true): Product = lock.withLock {
        val stored = repository.getProduct(product.id)
        if (!reviewedNow && (stored == null || stored.status != ListingStatus.PENDING_CONFIRM || stored.updatedAt != product.updatedAt)) {
            return@withLock stored ?: product
        }
        // Exact reviewed payload is durable before any network request.
        repository.saveProduct(product.copy(status = ListingStatus.PENDING_CONFIRM))
        try {
            api.confirmListing(product)
            val latest = repository.getProduct(product.id)
            // An artisan may edit while the request is in flight. Preserve that newer draft.
            val saved = if (latest != null && latest.updatedAt != product.updatedAt)
                latest.copy(remoteSaved = true)
            else product.copy(status = ListingStatus.SAVED, remoteSaved = true, lastError = null,
                updatedAt = System.currentTimeMillis())
            saved.also { repository.saveProduct(it) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val latest = repository.getProduct(product.id)
            if (latest == null || latest.updatedAt == product.updatedAt)
                repository.saveProduct(product.copy(status = ListingStatus.PENDING_CONFIRM,
                    lastError = "Cloud save failed. This reviewed listing will retry when connected."))
            throw e
        }
    }
}

class CatalogSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = KalaSetuRepository(KalaSetuDatabase.getDatabase(applicationContext))
        val engine = SyncEngine(repository, NetworkApiService(applicationContext))
        var failed = false
        repository.queueItems().filter { it.retryCount < 3 }.forEach { item ->
            try {
                val processed = engine.process(item.id)
                if (processed?.status == ListingStatus.DRAFT) com.example.core.service.ListingNotifications.show(applicationContext, item.id,
                    "Listing ready for review", "Review your listing before publishing.", "processing")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                failed = true
                if (item.retryCount >= 2) com.example.core.service.ListingNotifications.show(applicationContext, item.id,
                    "Listing needs attention", "Your media is safe. Open the sync center to retry.", "error")
            }
        }
        repository.allSnapshot().filter { it.status == ListingStatus.PENDING_CONFIRM }.forEach { product ->
            try {
                val confirmed = engine.confirm(product, reviewedNow = false)
                if (confirmed.status == ListingStatus.SAVED) com.example.core.service.ListingNotifications.show(applicationContext, product.id,
                    "Listing synced", "Your reviewed listing is now published.", "sync")
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; failed = true }
        }
        return if (failed && runAttemptCount < 2) Result.retry() else Result.success()
    }
    companion object {
        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
            WorkManager.getInstance(context).enqueueUniqueWork("catalog-sync", ExistingWorkPolicy.KEEP, request)
        }
    }
}
