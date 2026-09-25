package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.data.KalaSetuDatabase
import com.example.core.data.KalaSetuRepository
import com.example.core.model.*
import com.example.ui.screens.ReviewEditScreen
import com.example.ui.theme.KalaSetuTheme
import com.example.ui.viewmodel.KalaSetuViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProductDeviceTest {
    @get:Rule val compose = createComposeRule()
    @Test fun draftEditingPersistsAndPublicationRequiresValidPrice() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = KalaSetuViewModel(app, backgroundSync = false)
        val repo = KalaSetuRepository(KalaSetuDatabase.getDatabase(app))
        val product = Product(UUID.randomUUID().toString(), "Device test bowl", "Hand-carved bowl.",
            CraftCategory.WOOD, listOf("wood"), originalImageUrl = "https://example.test/photo.jpg",
            suggestedPrice = 100.0, finalPrice = 0.0, status = ListingStatus.DRAFT)
        vm.setDraftForReview(product)
        try {
            compose.setContent { KalaSetuTheme { ReviewEditScreen(vm, {}, {}) } }
            compose.onNodeWithText("Device test bowl").performTextReplacement("Edited device test bowl")
            compose.waitUntil(10_000) { runBlocking { repo.getProduct(product.id)?.title == "Edited device test bowl" } }
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Confirm and publish"))
            compose.onNodeWithText("Confirm and publish").assertIsNotEnabled()
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Your price (INR)"))
            compose.onNode(hasSetTextAction() and hasText("Your price (INR)")).performTextReplacement("450")
            compose.onNode(hasScrollAction()).performScrollToNode(hasText("Confirm and publish"))
            compose.onNodeWithText("Confirm and publish").assertIsEnabled()
            compose.waitUntil(10_000) { runBlocking { repo.getProduct(product.id)?.finalPrice == 450.0 } }
            assertEquals(ListingStatus.DRAFT, runBlocking { repo.getProduct(product.id)!!.status })
        } finally { runBlocking { repo.deleteProduct(product.id) } }
    }
}
