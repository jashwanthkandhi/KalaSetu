package com.example.core.network

import android.content.Context
import android.net.Uri
import com.example.core.model.*
import com.example.core.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.SecureRandom

class NetworkApiService(private val context: Context) : ApiService {
    companion object { private val identityLock = Any() }
    private val api get() = RetrofitClient.getApi()
    private val owner: String by lazy { synchronized(identityLock) {
        val prefs = context.getSharedPreferences("kalasetu_identity", Context.MODE_PRIVATE)
        prefs.getString("owner", null) ?: ByteArray(32).also { SecureRandom().nextBytes(it) }
            .joinToString("") { "%02x".format(it) }.also { check(prefs.edit().putString("owner", it).commit()) { "Could not preserve listing ownership." } }
    } }
    private fun bytes(uri: String): ByteArray {
        val result = if (uri.startsWith("content:") || uri.startsWith("file:"))
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
        else File(uri).takeIf { it.isFile }?.readBytes()
        return requireNotNull(result?.takeIf { it.isNotEmpty() }) { "Please select a photo again." }
    }
    private fun audio(path: String?) = MultipartBody.Part.createFormData("audio", "recording.m4a",
        requireNotNull(path) { "Please record a voice note." }.let { File(it).readBytes() }
            .also { require(it.isNotEmpty()) { "The recording is empty. Please record again." } }
            .toRequestBody("audio/mp4".toMediaType()))
    private fun <T> body(response: retrofit2.Response<T>): T {
        if (!response.isSuccessful) throw java.io.IOException("Service unavailable (${response.code()}). Please try again.")
        return response.body() ?: throw java.io.IOException("The server returned an incomplete response.")
    }
    override suspend fun processListing(photoUri: String, audioPath: String?, language: AppLanguage,
        onStageUpdate: (ProcessingStage) -> Unit): ProcessResponse = withContext(Dispatchers.IO) {
        onStageUpdate(ProcessingStage.TRANSCRIBING)
        val image = bytes(photoUri)
        val png = image.size > 4 && image[0] == 0x89.toByte() && image[1] == 0x50.toByte()
        val response = body(api.processListing(
            MultipartBody.Part.createFormData("photo", if (png) "product.png" else "product.jpg",
                image.toRequestBody((if (png) "image/png" else "image/jpeg").toMediaType())),
            audio(audioPath), language.code.toRequestBody("text/plain".toMediaType())))
        require(response.success) { response.error?.message ?: "Could not generate this listing." }
        val listing = requireNotNull(response.listing)
        ProcessResponse(response.request_id, response.transcript.orEmpty(), CraftCategory.fromString(listing.category),
            response.category?.confidence ?: 0f, requireNotNull(response.original_image_url),
            response.enhanced_image_url ?: requireNotNull(response.original_image_url), response.image_warning,
            listing.title, listing.description, listing.tags, listing.suggested_price, response.market_data, listing.attributes)
    }
    override suspend fun confirmListing(product: Product): String {
        val p = product
        val publicProfile = p.publicProfile.copy(email = "", photo = "",
            contact = if (p.publicProfile.contact_public) p.publicProfile.contact else "")
        val result = body(api.confirmListing(owner, ConfirmListingApiRequest(p.id, p.originalImageUrl, p.imageUrl,
            p.imageWarning, p.voiceTranscript, p.title, p.description, p.category.displayName, p.tags,
            p.finalPrice, p.suggestedPrice, p.attributes, p.marketData, p.languageCode, publicProfile,
            if (p.status == ListingStatus.ARCHIVED) "archived" else "saved")))
        check(result.success && result.product_id == p.id) { "Cloud save was not confirmed." }
        return requireNotNull(result.product_id)
    }
    override suspend fun catalog() = body(api.catalog(owner)).products.map { it.toProduct() }
    override suspend fun discover(offset: Int) = body(api.discover(offset)).products.map { it.toProduct() }
    override suspend fun delete(product: Product) { body(api.delete(owner, product.id)) }
    override suspend fun assist(product: Product, instruction: String, language: AppLanguage): Product {
        val p = product
        val result = body(api.assist(AssistApiRequest(ListingApiDto(p.title, p.description,
            p.category.displayName, p.tags, p.suggestedPrice, p.attributes), instruction, language.code))).listing
        return p.copy(title = result.title, description = result.description, category = CraftCategory.fromString(result.category),
            tags = result.tags, suggestedPrice = result.suggested_price, attributes = result.attributes)
    }
    override suspend fun voiceEdit(path: String, language: AppLanguage) = body(api.voiceEdit(audio(path),
        language.code.toRequestBody("text/plain".toMediaType()))).transcript
    override suspend fun readAloud(text: String, language: AppLanguage) = body(api.tts(TtsApiRequest(text.take(500), language.code))).bytes()
}
