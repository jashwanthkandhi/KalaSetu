package com.example.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.core.model.CraftCategory
import com.example.core.model.ListingStatus
import com.example.core.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val tagsString: String, // comma-separated
    val imageUrl: String?,
    val localImageUri: String?,
    val sampleDrawableRes: Int?,
    val originalImageUrl: String?,
    val voiceTranscript: String?,
    val suggestedPrice: Double,
    val finalPrice: Double,
    val status: String,
    val createdAt: Long
) {
    fun toDomain(): Product {
        return Product(
            id = id,
            title = title,
            description = description,
            category = CraftCategory.fromString(category),
            tags = if (tagsString.isBlank()) emptyList() else tagsString.split("||").filter { it.isNotBlank() },
            imageUrl = imageUrl,
            localImageUri = localImageUri,
            sampleDrawableRes = sampleDrawableRes,
            originalImageUrl = originalImageUrl,
            voiceTranscript = voiceTranscript,
            suggestedPrice = suggestedPrice,
            finalPrice = finalPrice,
            status = try { ListingStatus.valueOf(status) } catch (e: Exception) { ListingStatus.SAVED },
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                title = product.title,
                description = product.description,
                category = product.category.displayName,
                tagsString = product.tags.joinToString("||"),
                imageUrl = product.imageUrl,
                localImageUri = product.localImageUri,
                sampleDrawableRes = product.sampleDrawableRes,
                originalImageUrl = product.originalImageUrl,
                voiceTranscript = product.voiceTranscript,
                suggestedPrice = product.suggestedPrice,
                finalPrice = product.finalPrice,
                status = product.status.name,
                createdAt = product.createdAt
            )
        }
    }
}

@Entity(tableName = "offline_queue")
data class OfflineQueueEntity(
    @PrimaryKey val id: String,
    val photoUri: String,
    val audioPath: String?,
    val languageCode: String,
    val createdAt: Long,
    val status: String,
    val retryCount: Int,
    val lastError: String?
)
