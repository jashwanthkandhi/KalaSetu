package com.example.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val productAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(Product::class.java)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String, val description: String, val category: String, val tagsString: String,
    val imageUrl: String?, val localImageUri: String?, val sampleDrawableRes: Int?,
    val originalImageUrl: String?, val voiceTranscript: String?,
    val suggestedPrice: Double, val finalPrice: Double, val status: String, val createdAt: Long,
    @androidx.room.ColumnInfo(defaultValue = "'{}'") val metadata: String = "{}"
) {
    fun toDomain(): Product {
        val legacy = Product(id, title, description, CraftCategory.fromString(category),
            tagsString.split("||").filter { it.isNotBlank() }, imageUrl, localImageUri,
            sampleDrawableRes, originalImageUrl, voiceTranscript, suggestedPrice, finalPrice,
            runCatching { ListingStatus.valueOf(status) }.getOrDefault(ListingStatus.DRAFT), createdAt)
        return runCatching { productAdapter.fromJson(metadata) }.getOrNull() ?: legacy
    }
    companion object {
        fun fromDomain(p: Product) = ProductEntity(p.id, p.title, p.description, p.category.displayName,
            p.tags.joinToString("||"), p.imageUrl, p.localImageUri, p.sampleDrawableRes,
            p.originalImageUrl, p.voiceTranscript, p.suggestedPrice, p.finalPrice, p.status.name,
            p.createdAt, productAdapter.toJson(p))
    }
}

@Entity(tableName = "offline_queue")
data class OfflineQueueEntity(
    @PrimaryKey val id: String, val photoUri: String, val audioPath: String?,
    val languageCode: String, val createdAt: Long, val status: String,
    val retryCount: Int, val lastError: String?
)
