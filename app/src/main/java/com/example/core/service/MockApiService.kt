package com.example.core.service

import com.example.core.model.AppLanguage
import com.example.core.model.CraftCategory
import com.example.core.model.ProcessResponse
import com.example.core.model.ProcessingStage
import com.example.core.model.Product
import kotlinx.coroutines.delay
import java.util.UUID

interface ApiService {
    suspend fun processListing(
        photoUri: String,
        audioPath: String?,
        language: AppLanguage,
        onStageUpdate: (ProcessingStage) -> Unit
    ): ProcessResponse

    suspend fun confirmListing(
        product: Product
    ): String
}

class MockApiService : ApiService {

    override suspend fun processListing(
        photoUri: String,
        audioPath: String?,
        language: AppLanguage,
        onStageUpdate: (ProcessingStage) -> Unit
    ): ProcessResponse {
        // Stage 1: Understanding voice (Transcribing 🎙️)
        onStageUpdate(ProcessingStage.TRANSCRIBING)
        delay(1600)

        val transcript = when (language) {
            AppLanguage.TELUGU -> "ఇది మా చేతితో చేసిన సాంప్రదాయ మట్టి కుండ, పూల నగిషీలతో కూడిన సహజమైన క్లే వాజ్."
            AppLanguage.HINDI -> "यह शुद्ध प्राकृतिक मिट्टी से बनी पारंपरिक हस्तनिर्मित सुराही है, जिस पर सुंदर नक्काशी है।"
            AppLanguage.ENGLISH -> "This is a traditional handmade terracotta vase crafted from pure river clay with hand-carved floral motifs."
        }

        // Stage 2: Enhancing product photo (Enhancing image 🖼️)
        onStageUpdate(ProcessingStage.ENHANCING_IMAGE)
        delay(1800)

        // Stage 3: Identifying your craft (Categorising 🏷️)
        onStageUpdate(ProcessingStage.CATEGORISING)
        delay(1400)

        val category = CraftCategory.POTTERY
        val confidence = 0.94f

        // Stage 4: Creating your listing (Generating listing ✨)
        onStageUpdate(ProcessingStage.GENERATING_LISTING)
        delay(1600)

        val title = when (language) {
            AppLanguage.TELUGU -> "హ్యాండ్‌క్రాఫ్టెడ్ టెర్రకోట వాటర్ పాట్"
            AppLanguage.HINDI -> "हस्तनिर्मित टेराकोटा वाटर पॉट"
            AppLanguage.ENGLISH -> "Handcrafted Terracotta Water Pot"
        }

        val description = when (language) {
            AppLanguage.TELUGU -> "రోజువారీ ఉపయోగం కోసం తయారు చేసిన సాంప్రదాయ చేతితో చేసిన మట్టి కుండ. సహజ బంకమట్టి, పర్యావరణ అనుకూలం."
            AppLanguage.HINDI -> "दैनिक उपयोग के लिए पारंपरिक हस्तनिर्मित मिट्टी का बर्तन। प्राकृतिक मिट्टी, पर्यावरण अनुकूल।"
            AppLanguage.ENGLISH -> "Traditional handmade terracotta pot designed for everyday use. Natural clay, eco-friendly."
        }

        val tags = listOf("terracotta", "handmade", "kitchen")
        val suggestedPrice = 450.0

        onStageUpdate(ProcessingStage.COMPLETED)

        return ProcessResponse(
            requestId = UUID.randomUUID().toString(),
            transcript = transcript,
            category = category,
            categoryConfidence = confidence,
            originalImageUrl = photoUri,
            enhancedImageUrl = photoUri,
            imageWarning = false,
            title = title,
            description = description,
            tags = tags,
            suggestedPrice = suggestedPrice
        )
    }

    override suspend fun confirmListing(product: Product): String {
        delay(500)
        return product.id
    }
}
