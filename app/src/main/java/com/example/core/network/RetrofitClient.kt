package com.example.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 connects from Android emulator to host machine localhost:8000
    private val DEFAULT_BASE_URL = com.example.BuildConfig.BACKEND_URL

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Volatile
    private var instance: KalaSetuApi? = null
    private var currentBaseUrl = DEFAULT_BASE_URL

    fun getApi(baseUrl: String = DEFAULT_BASE_URL): KalaSetuApi {
        if (instance == null || currentBaseUrl != baseUrl) {
            synchronized(this) {
                if (instance == null || currentBaseUrl != baseUrl) {
                    currentBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                    instance = Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                        .create(KalaSetuApi::class.java)
                }
            }
        }
        return instance!!
    }
}
