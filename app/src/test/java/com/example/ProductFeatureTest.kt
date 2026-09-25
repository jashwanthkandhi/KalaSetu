package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.data.*
import com.example.core.model.*
import com.example.core.service.ApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

internal fun testProduct(id: String = "00000000-0000-0000-0000-000000000001", status: ListingStatus = ListingStatus.DRAFT) = Product(
    id, "Carved wooden bowl", "Hand-carved bowl in neem wood.", CraftCategory.WOOD, listOf("carved", "bowl"),
    originalImageUrl = "https://storage.test/original.jpg", imageUrl = "https://storage.test/enhanced.jpg",
    suggestedPrice = 850.0, finalPrice = 950.0, status = status, createdAt = 100,
    marketData = MarketData(source = "google_shopping", low = 800.0, high = 1200.0), attributes = mapOf("material" to "neem wood"))

internal class TestApi : ApiService {
    var failProcess = false
    var failConfirm = false
    var confirmations = 0
    var duringConfirm: (suspend () -> Unit)? = null
    override suspend fun processListing(photoUri: String, audioPath: String?, language: AppLanguage, onStageUpdate: (ProcessingStage) -> Unit): ProcessResponse {
        if (failProcess) throw java.io.IOException("offline")
        return ProcessResponse("server-request", "wooden bowl", CraftCategory.WOOD, .8f,
            "https://storage.test/original.jpg", "https://storage.test/enhanced.jpg", false,
            "Wooden bowl", "Hand-carved bowl.", listOf("wood"), 850.0)
    }
    override suspend fun confirmListing(product: Product): String { confirmations++; duringConfirm?.invoke(); if (failConfirm) throw java.io.IOException("offline"); return product.id }
    override suspend fun catalog() = emptyList<Product>()
    override suspend fun discover(offset: Int) = emptyList<Product>()
    override suspend fun delete(product: Product) {}
    override suspend fun assist(product: Product, instruction: String, language: AppLanguage) = product.copy(title = "Proposed title")
    override suspend fun voiceEdit(path: String, language: AppLanguage) = "Change the title"
    override suspend fun readAloud(text: String, language: AppLanguage) = byteArrayOf()
}

class ProductFeatureTest {
    @Test fun `search matches title category and tags without case sensitivity`() {
        val product = testProduct()
        for (query in listOf("carved", "WOOD", "bowl")) assertEquals(1, filterCatalog(listOf(product), query).size)
        assertTrue(filterCatalog(listOf(product), "pottery").isEmpty())
    }
    @Test fun `filters and price sorting compose correctly`() {
        val a = testProduct("a").copy(finalPrice = 100.0, createdAt = 10)
        val b = testProduct("b", ListingStatus.SAVED).copy(finalPrice = 200.0, createdAt = 20)
        assertEquals(listOf(b, a), filterCatalog(listOf(a, b), sort = CatalogSort.PRICE_HIGH))
        assertEquals(listOf(a), filterCatalog(listOf(a, b), status = ListingStatus.DRAFT))
        assertTrue(filterCatalog(listOf(a, b), category = CraftCategory.POTTERY).isEmpty())
    }
    @Test fun `statistics never count pending cloud saves as published`() {
        val stats = catalogStats(listOf(testProduct(), testProduct("b", ListingStatus.PENDING_CONFIRM), testProduct("c", ListingStatus.FAILED)), 1000)
        assertEquals(0, stats.published); assertEquals(1, stats.drafts); assertEquals(1, stats.pending); assertEquals(1, stats.failed)
    }
    @Test fun `quality rejects invalid price and missing media`() {
        assertTrue(testProduct().qualityIssues().isEmpty())
        for (price in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 50001.0)) assertTrue(testProduct().copy(finalPrice = price).qualityIssues().isNotEmpty())
        assertTrue(testProduct().copy(title = "", originalImageUrl = null, tags = emptyList()).qualityIssues().size >= 3)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PersistenceSyncTest {
    private fun database(): KalaSetuDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KalaSetuDatabase::class.java).allowMainThreadQueries().build()
    @Test fun `room round trip retains new metadata`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db)
            val p = testProduct().copy(imageWarning = true, languageCode = "hi", publicProfile = ArtisanProfile(shop_name = "Wood studio"))
            repo.saveProduct(p)
            assertEquals(p, repo.getProduct(p.id))
        } finally { db.close() }
    }
    @Test fun `offline generation preserves ID and requires review without confirming`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val api = TestApi(); val p = testProduct(status = ListingStatus.PENDING_UPLOAD)
            repo.enqueue(p, OfflineQueueEntity(p.id, "photo", "audio", "hi", 100, "queued", 0, null))
            val result = SyncEngine(repo, api).process(p.id)!!
            assertEquals(p.id, result.id); assertEquals(ListingStatus.DRAFT, result.status)
            assertEquals(0, api.confirmations); assertTrue(repo.queueItems().isEmpty())
            assertEquals("hi", result.languageCode)
        } finally { db.close() }
    }
    @Test fun `failed processing retains media and increments attempts`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val api = TestApi().apply { failProcess = true }; val p = testProduct()
            repo.enqueue(p, OfflineQueueEntity(p.id, "photo", "audio", "en", 100, "queued", 0, null))
            runCatching { SyncEngine(repo, api).process(p.id) }
            assertEquals(ListingStatus.FAILED, repo.getProduct(p.id)!!.status)
            assertEquals(1, repo.queueItems().single().retryCount)
            assertEquals("audio", repo.queueItems().single().audioPath)
            api.failProcess = false
            SyncEngine(repo, api).process(p.id)
            assertEquals(ListingStatus.DRAFT, repo.getProduct(p.id)!!.status)
        } finally { db.close() }
    }
    @Test fun `failed confirmation stays pending until successful retry`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val api = TestApi().apply { failConfirm = true }; val p = testProduct()
            val engine = SyncEngine(repo, api)
            runCatching { engine.confirm(p) }
            val pending = repo.getProduct(p.id)!!
            assertEquals(ListingStatus.PENDING_CONFIRM, pending.status); assertFalse(pending.remoteSaved)
            api.failConfirm = false
            engine.confirm(pending)
            assertEquals(ListingStatus.SAVED, repo.getProduct(p.id)!!.status)
            assertTrue(repo.getProduct(p.id)!!.remoteSaved)
            assertEquals(1, repo.allSnapshot().size)
        } finally { db.close() }
    }
    @Test fun `delete removes queued work atomically`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val p = testProduct()
            repo.enqueue(p, OfflineQueueEntity(p.id, "photo", "audio", "en", 100, "queued", 0, null))
            repo.deleteProduct(p.id)
            assertNull(repo.getProduct(p.id)); assertTrue(repo.queueItems().isEmpty())
        } finally { db.close() }
    }
    @Test fun `interrupted upload becomes retryable without losing media`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val p = testProduct(status = ListingStatus.UPLOADING)
            repo.enqueue(p, OfflineQueueEntity(p.id, "photo", "audio", "en", 100, "uploading", 1, null))
            repo.recoverInterrupted()
            assertEquals(ListingStatus.PENDING_UPLOAD, repo.getProduct(p.id)!!.status)
            assertEquals("queued", repo.queueItems().single().status)
            assertEquals("audio", repo.queueItems().single().audioPath)
        } finally { db.close() }
    }
    @Test fun `stale worker cannot publish a newer unreviewed draft`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val api = TestApi()
            val pending = testProduct(status = ListingStatus.PENDING_CONFIRM).copy(updatedAt = 100)
            val edited = pending.copy(title = "My newer title", status = ListingStatus.DRAFT, updatedAt = 101)
            repo.saveProduct(edited)
            SyncEngine(repo, api).confirm(pending, reviewedNow = false)
            assertEquals(0, api.confirmations); assertEquals(edited, repo.getProduct(pending.id))
        } finally { db.close() }
    }
    @Test fun `cloud acknowledgement preserves edits made during request`() = runBlocking {
        val db = database()
        try {
            val repo = KalaSetuRepository(db); val api = TestApi()
            val p = testProduct().copy(updatedAt = 100)
            api.duringConfirm = { repo.saveProduct(p.copy(title = "New title", updatedAt = 101)) }
            SyncEngine(repo, api).confirm(p)
            val saved = repo.getProduct(p.id)!!
            assertEquals("New title", saved.title); assertEquals(ListingStatus.DRAFT, saved.status)
            assertTrue(saved.remoteSaved)
        } finally { db.close() }
    }
    @Test fun `version one migration preserves products and queue without destructive fallback`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-proof"
        context.deleteDatabase(name)
        val old = context.openOrCreateDatabase(name, 0, null)
        old.execSQL("CREATE TABLE products (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, category TEXT NOT NULL, tagsString TEXT NOT NULL, imageUrl TEXT, localImageUri TEXT, sampleDrawableRes INTEGER, originalImageUrl TEXT, voiceTranscript TEXT, suggestedPrice REAL NOT NULL, finalPrice REAL NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL)")
        old.execSQL("CREATE TABLE offline_queue (id TEXT NOT NULL PRIMARY KEY, photoUri TEXT NOT NULL, audioPath TEXT, languageCode TEXT NOT NULL, createdAt INTEGER NOT NULL, status TEXT NOT NULL, retryCount INTEGER NOT NULL, lastError TEXT)")
        old.execSQL("INSERT INTO products VALUES ('legacy','Keep my work','Description','Wood','wood',NULL,'file:///photo',NULL,NULL,NULL,100,100,'SAVED',100)")
        old.execSQL("INSERT INTO offline_queue VALUES ('legacy','file:///photo','audio','en',100,'uploading',1,NULL)")
        old.version = 1; old.close()
        val db = Room.databaseBuilder(context, KalaSetuDatabase::class.java, name).addMigrations(KalaSetuDatabase.MIGRATION_1_2).allowMainThreadQueries().build()
        try { runBlocking {
            assertEquals("Keep my work", db.productDao().getProductById("legacy")!!.title)
            assertEquals("DRAFT", db.productDao().getProductById("legacy")!!.status)
            assertEquals("queued", db.offlineQueueDao().snapshot().single().status)
        } } finally { db.close(); context.deleteDatabase(name) }
    }
}
