package com.example.core.service

import com.example.core.model.*

interface ApiService {
    suspend fun processListing(photoUri: String, audioPath: String?, language: AppLanguage,
                               onStageUpdate: (ProcessingStage) -> Unit): ProcessResponse
    suspend fun confirmListing(product: Product): String
    suspend fun catalog(): List<Product>
    suspend fun discover(offset: Int = 0): List<Product>
    suspend fun delete(product: Product)
    suspend fun assist(product: Product, instruction: String, language: AppLanguage): Product
    suspend fun voiceEdit(path: String, language: AppLanguage): String
    suspend fun readAloud(text: String, language: AppLanguage): ByteArray
}
