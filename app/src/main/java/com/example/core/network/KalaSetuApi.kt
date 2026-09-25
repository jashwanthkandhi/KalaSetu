package com.example.core.network

import com.example.core.model.*
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class CategoryApiDto(val name: String, val confidence: Float)
@JsonClass(generateAdapter = true)
data class ListingApiDto(val title: String, val description: String, val category: String,
    val tags: List<String> = emptyList(), val suggested_price: Double,
    val attributes: Map<String, String> = emptyMap(), val final_price: Double? = null)
@JsonClass(generateAdapter = true)
data class ErrorApiDto(val code: String, val message: String)
@JsonClass(generateAdapter = true)
data class ProcessApiResponse(val success: Boolean, val request_id: String,
    val transcript: String? = null, val category: CategoryApiDto? = null,
    val original_image_url: String? = null, val enhanced_image_url: String? = null,
    val image_warning: Boolean = false, val listing: ListingApiDto? = null,
    val error: ErrorApiDto? = null, val market_data: MarketData = MarketData())
@JsonClass(generateAdapter = true)
data class ConfirmListingApiRequest(val product_id: String,
    val original_image_url: String?, val enhanced_image_url: String?, val image_warning: Boolean,
    val transcript: String?, val title: String, val description: String, val category: String,
    val tags: List<String>, val final_price: Double, val suggested_price: Double,
    val attributes: Map<String, String>, val market_data: MarketData, val language: String,
    val public_profile: ArtisanProfile, val status: String = "saved")
@JsonClass(generateAdapter = true)
data class ConfirmApiResponse(val success: Boolean, val product_id: String? = null, val error: ErrorApiDto? = null)
@JsonClass(generateAdapter = true)
data class RemoteProduct(val id: String, val title: String, val description: String, val category: String,
    val tags: List<String> = emptyList(), val original_image_url: String? = null,
    val enhanced_image_url: String? = null, val final_price: Double, val suggested_price: Double = 0.0,
    val voice_transcript: String? = null, val status: String = "saved", val language: String = "en",
    val image_warning: Boolean = false, val created_at: String, val updated_at: String? = null,
    val attributes: Map<String, String> = emptyMap(), val market_data: MarketData = MarketData(),
    val public_profile: ArtisanProfile = ArtisanProfile()) {
    fun toProduct() = Product(id, title, description, CraftCategory.fromString(category), tags,
        imageUrl = enhanced_image_url, originalImageUrl = original_image_url, voiceTranscript = voice_transcript,
        suggestedPrice = suggested_price, finalPrice = final_price,
        status = if (status == "archived") ListingStatus.ARCHIVED else ListingStatus.SAVED,
        createdAt = runCatching { java.time.Instant.parse(created_at).toEpochMilli() }.getOrDefault(0),
        updatedAt = runCatching { java.time.Instant.parse(updated_at ?: created_at).toEpochMilli() }.getOrDefault(0),
        languageCode = language, imageWarning = image_warning, marketData = market_data,
        attributes = attributes, publicProfile = public_profile, remoteSaved = true)
}
@JsonClass(generateAdapter = true)
data class CatalogApiResponse(val products: List<RemoteProduct>)
@JsonClass(generateAdapter = true)
data class AssistApiRequest(val listing: ListingApiDto, val instruction: String, val language: String)
@JsonClass(generateAdapter = true)
data class AssistApiResponse(val success: Boolean, val listing: ListingApiDto)
@JsonClass(generateAdapter = true)
data class VoiceEditResponse(val transcript: String)
@JsonClass(generateAdapter = true)
data class TtsApiRequest(val text: String, val language: String)

interface KalaSetuApi {
    @POST("api/v1/distribution/social-content")
    suspend fun socialContent(@Body request: SocialRequest): Response<SocialContent>
    @Multipart @POST("api/v1/listings/jobs")
    suspend fun createJob(@Header("X-Owner-Key") owner: String, @Header("Idempotency-Key") key: String,
        @Part photo: MultipartBody.Part, @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody): Response<JobAccepted>
    @GET("api/v1/listings/jobs/{id}")
    suspend fun job(@Header("X-Owner-Key") owner: String, @Path("id") id: String): Response<JobProgress>
    @Multipart @POST("api/v1/listings/process")
    suspend fun processListing(@Part photo: MultipartBody.Part, @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody): Response<ProcessApiResponse>
    @POST("api/v1/listings/confirm")
    suspend fun confirmListing(@Header("X-Owner-Key") owner: String, @Body request: ConfirmListingApiRequest): Response<ConfirmApiResponse>
    @GET("api/v1/listings")
    suspend fun catalog(@Header("X-Owner-Key") owner: String): Response<CatalogApiResponse>
    @GET("api/v1/listings/discover")
    suspend fun discover(@Query("offset") offset: Int = 0): Response<CatalogApiResponse>
    @DELETE("api/v1/listings/{id}")
    suspend fun delete(@Header("X-Owner-Key") owner: String, @Path("id") id: String): Response<ResponseBody>
    @POST("api/v1/listings/assist")
    suspend fun assist(@Body request: AssistApiRequest): Response<AssistApiResponse>
    @Multipart @POST("api/v1/listings/voice-edit")
    suspend fun voiceEdit(@Part audio: MultipartBody.Part, @Part("language") language: RequestBody): Response<VoiceEditResponse>
    @POST("api/v1/tts")
    suspend fun tts(@Body request: TtsApiRequest): Response<ResponseBody>
}

@JsonClass(generateAdapter = true)
data class JobAccepted(val job_id: String, val poll_url: String, val state: String)
@JsonClass(generateAdapter = true)
data class JobProgress(val id: String, val state: String, val result: ProcessApiResponse? = null,
    val error: ErrorApiDto? = null)
@JsonClass(generateAdapter = true)
data class SocialRequest(val listing: ListingApiDto, val language: String)
@JsonClass(generateAdapter = true)
data class SocialContent(val caption: String, val story: String, val hashtags: List<String>, val call_to_action: String)
