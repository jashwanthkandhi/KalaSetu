package com.example.core.model

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    TELUGU("te", "తెలుగు", "Telugu"),
    HINDI("hi", "हिंदी", "Hindi"),
    ENGLISH("en", "English", "English")
}

enum class CraftCategory(val displayName: String, val iconRes: String) {
    POTTERY("Pottery", "pottery"),
    TEXTILES("Textiles", "textiles"),
    BAMBOO("Bamboo", "bamboo"),
    WOOD("Wood", "wood"),
    HOME_DECOR("Home Decor", "home_decor"),
    JEWELLERY("Jewellery", "jewellery"),
    PAINTINGS("Paintings", "paintings"),
    LEATHER("Leather", "leather"),
    OTHER("Other", "other");

    companion object {
        fun fromString(value: String): CraftCategory {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class ListingStatus(val label: String) {
    SAVED("Saved"),
    DRAFT("Draft"),
    PENDING_UPLOAD("Pending Upload"),
    UPLOADING("Uploading")
}

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val category: CraftCategory,
    val tags: List<String>,
    val imageUrl: String? = null,
    val localImageUri: String? = null,
    val sampleDrawableRes: Int? = null,
    val originalImageUrl: String? = null,
    val voiceTranscript: String? = null,
    val suggestedPrice: Double,
    val finalPrice: Double,
    val status: ListingStatus = ListingStatus.SAVED,
    val createdAt: Long = System.currentTimeMillis()
)

data class ProcessResponse(
    val requestId: String,
    val transcript: String,
    val category: CraftCategory,
    val categoryConfidence: Float,
    val originalImageUrl: String,
    val enhancedImageUrl: String,
    val imageWarning: Boolean = false,
    val title: String,
    val description: String,
    val tags: List<String>,
    val suggestedPrice: Double
)

enum class ProcessingStage(val stepNumber: Int) {
    TRANSCRIBING(1),
    ENHANCING_IMAGE(2),
    CATEGORISING(3),
    GENERATING_LISTING(4),
    COMPLETED(5),
    FAILED(-1)
}

data class OfflineQueueItem(
    val id: String,
    val photoUri: String,
    val audioPath: String?,
    val languageCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "queued", // queued | uploading | failed | dead
    val retryCount: Int = 0,
    val lastError: String? = null
)
