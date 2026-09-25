package com.example.core.model

import java.math.BigDecimal
import java.math.RoundingMode

data class ArtisanCosts(val hours: Double, val hourlyRate: Double, val materials: Double, val markup: Double) {
    fun calculate(): MarketData {
        require(listOf(hours, hourlyRate, materials, markup).all { it.isFinite() })
        require(hours in 0.0..10000.0 && hourlyRate > 0 && hourlyRate <= 50000 && materials in 0.0..50000.0 && markup in 0.0..200.0)
        val labor = BigDecimal.valueOf(hours).multiply(BigDecimal.valueOf(hourlyRate))
        val base = labor.add(BigDecimal.valueOf(materials))
        val margin = base.multiply(BigDecimal.valueOf(markup)).divide(BigDecimal(100))
        val total = base.add(margin).setScale(2, RoundingMode.HALF_UP).toDouble()
        require(total > 0 && total <= 50000)
        return MarketData(source = "artisan_cost_plus", low = total, high = total,
            explanation = "${hours} h × ₹${hourlyRate} + ₹${materials} + ${markup}% markup. Taxes and delivery excluded.",
            labor_cost = labor.setScale(2, RoundingMode.HALF_UP).toDouble(), material_cost = materials,
            margin = margin.setScale(2, RoundingMode.HALF_UP).toDouble())
    }
}

fun reviewSummary(product: Product, language: AppLanguage): String {
    val price = if (product.finalPrice > 0) "₹${product.finalPrice}" else when(language) {
        AppLanguage.TELUGU -> "ధర ఇంకా నిర్ణయించలేదు"
        AppLanguage.HINDI -> "कीमत अभी तय नहीं है"
        AppLanguage.ENGLISH -> "price not set yet"
    }
    return when(language) {
        AppLanguage.TELUGU -> "మీ ${product.title} లిస్టింగ్ సిద్ధంగా ఉంది. $price. వివరాలను సమీక్షించండి. ప్రచురించాలా?"
        AppLanguage.HINDI -> "आपकी ${product.title} लिस्टिंग तैयार है। $price। विवरण जाँचें। क्या इसे प्रकाशित करें?"
        AppLanguage.ENGLISH -> "Your ${product.title} listing is ready. $price. Please review the details. Would you like to publish it?"
    }
}
