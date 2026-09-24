package com.example.core.network

import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@JsonClass(generateAdapter = true)
data class CategoryApiDto(
    val name: String,
    val confidence: Float
)

@JsonClass(generateAdapter = true)
data class ListingApiDto(
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val suggested_price: Double
)

@JsonClass(generateAdapter = true)
data class ErrorApiDto(
    val code: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class ProcessApiResponse(
    val success: Boolean = true,
    val request_id: String,
    val transcript: String? = null,
    val category: CategoryApiDto? = null,
    val original_image_url: String? = null,
    val enhanced_image_url: String? = null,
    val image_warning: Boolean = false,
    val listing: ListingApiDto? = null,
    val error: ErrorApiDto? = null
)

@JsonClass(generateAdapter = true)
data class ConfirmListingApiRequest(
    val artisan_id: String? = null,
    val original_image_url: String? = null,
    val enhanced_image_url: String? = null,
    val image_warning: Boolean = false,
    val transcript: String? = null,
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val final_price: Double,
    val suggested_price: Double
)

@JsonClass(generateAdapter = true)
data class ConfirmApiResponse(
    val success: Boolean = true,
    val product_id: String? = null,
    val error: ErrorApiDto? = null
)

@JsonClass(generateAdapter = true)
data class HealthApiResponse(
    val status: String,
    val model: String? = null,
    val stack: String? = null,
    val mock_mode: Boolean = false
)

interface KalaSetuApi {
    @Multipart
    @POST("api/v1/listings/process")
    suspend fun processListing(
        @Part photo: MultipartBody.Part,
        @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody
    ): Response<ProcessApiResponse>

    @POST("api/v1/listings/confirm")
    suspend fun confirmListing(
        @Body request: ConfirmListingApiRequest
    ): Response<ConfirmApiResponse>

    @GET("health")
    suspend fun healthCheck(): Response<HealthApiResponse>
}
