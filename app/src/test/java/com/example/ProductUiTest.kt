package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.core.data.KalaSetuDatabase
import com.example.core.data.KalaSetuRepository
import com.example.core.model.*
import com.example.ui.KalaSetuApp
import com.example.ui.viewmodel.KalaSetuViewModel
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-420dpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProductUiTest {
    @get:Rule val compose = createComposeRule()
    private fun start(dark: Boolean = false, language: String = "en"): KalaSetuViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("kalasetu_prefs", Context.MODE_PRIVATE).edit().clear()
            .putBoolean("tutorial_seen", true).putString("selected_language", language).commit()
        val vm = KalaSetuViewModel(app, TestApi(), backgroundSync = false)
        vm.updateProfile(ArtisanProfile(display_name = "Ananya", shop_name = "Earth & Thread", craft = "Woodcraft"))
        if (dark) vm.updatePreferences(AppPreferences(theme = "Dark", largerText = true, highContrast = true))
        runBlocking { KalaSetuRepository(KalaSetuDatabase.getDatabase(app)).saveProduct(testProduct()) }
        compose.setContent { KalaSetuApp(vm) }
        compose.waitForIdle()
        return vm
    }
    @Test fun `home catalog detail review and settings navigate coherently`() {
        start()
        compose.onNodeWithText("Create listing").assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/home.png")
        compose.onAllNodesWithText("Catalog").onLast().performClick()
        compose.onNodeWithTag("catalog_search").performTextInput("carved")
        compose.onNodeWithText("Carved wooden bowl").assertExists().performClick()
        compose.onRoot().captureRoboImage("build/reports/product-ui/detail.png")
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Edit"))
        compose.onNodeWithText("Edit").performClick()
        compose.onAllNodesWithText("Review listing").onFirst().assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/review.png")
    }
    @Test fun `dark high contrast large type home renders`() {
        start(dark = true)
        compose.onRoot().captureRoboImage("build/reports/product-ui/home-dark-large.png")
        compose.onNodeWithText("Profile").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Settings"))
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Appearance").assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/settings-dark-large.png")
    }
    @Test fun `capture cannot generate without real media`() {
        start()
        compose.onNodeWithText("Create", useUnmergedTree = true).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Generate listing"))
        compose.onNodeWithText("Generate listing").assertIsNotEnabled()
        compose.onRoot().captureRoboImage("build/reports/product-ui/capture.png")
    }
    @Test fun `catalog view switch and filter reset retain real products`() {
        start()
        compose.onAllNodesWithText("Catalog").onLast().performClick()
        compose.onNodeWithTag("catalog_search").performTextInput("no match")
        compose.onNodeWithText("Reset filters").performClick()
        compose.onNodeWithText("Carved wooden bowl").assertExists()
        compose.onNodeWithContentDescription("List view").performClick()
        compose.onNodeWithContentDescription("Grid view").assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/catalog-list.png")
    }
    @Test fun `capture guidance opens and dismisses without losing screen`() {
        start()
        compose.onNodeWithText("Create", useUnmergedTree = true).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Tips for a beautiful listing"))
        compose.onNodeWithText("Tips for a beautiful listing").performClick()
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithTag("capture_screen").assertExists()
    }
    @Test fun `telugu interactive home renders`() {
        start(language = "te")
        compose.onNodeWithText("మీ సృష్టి. ప్రపంచంతో పంచుకోండి.").assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/home-telugu.png")
    }
    @Test fun `hindi interactive home renders with large text`() {
        start(dark = true, language = "hi")
        compose.onNodeWithText("आपकी रचना। दुनिया के साथ साझा करें।").assertExists()
        compose.onRoot().captureRoboImage("build/reports/product-ui/home-hindi-large.png")
    }
    @Test fun `profile preferences and favorites survive viewmodel recreation`() {
        val vm = start()
        val profile = ArtisanProfile(display_name = "Artisan", contact = "12345", contact_public = false)
        val preferences = AppPreferences(theme = "Dark", speechSpeed = 0.75f, processingNotifications = false)
        compose.runOnIdle {
            vm.updateProfile(profile); vm.updatePreferences(preferences); vm.toggleFavorite("favorite-test")
        }
        val restored = KalaSetuViewModel(ApplicationProvider.getApplicationContext(), TestApi(), backgroundSync = false)
        assertEquals(profile, restored.profile.value)
        assertEquals(preferences, restored.preferences.value)
        assertTrue("favorite-test" in restored.favorites.value)
    }
    @Test fun `assistant requires approval and duplicate has separate identity`() {
        val vm = start()
        compose.runOnIdle { vm.setDraftForReview(testProduct()); vm.requestAssistant("Improve title") }
        compose.waitUntil(10_000) { vm.assistantProposal.value != null }
        assertEquals("Carved wooden bowl", vm.currentDraft.value!!.title)
        compose.runOnIdle { vm.applyAssistantProposal() }
        assertEquals("Proposed title", vm.currentDraft.value!!.title)
        assertEquals(950.0, vm.currentDraft.value!!.finalPrice, 0.0)
        val original = vm.currentDraft.value!!
        compose.waitUntil(10_000) { !vm.busy.value }
        compose.runOnIdle { vm.duplicateProduct(original) {} }
        compose.waitUntil(10_000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            vm.currentDraft.value?.id != original.id
        }
        compose.waitUntil(10_000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            !vm.busy.value
        }
        val copy = vm.currentDraft.value!!
        assertEquals(ListingStatus.DRAFT, copy.status); assertFalse(copy.remoteSaved)
        val repo = KalaSetuRepository(KalaSetuDatabase.getDatabase(ApplicationProvider.getApplicationContext()))
        assertEquals(copy, runBlocking { repo.getProduct(copy.id) })
        assertNotNull(runBlocking { repo.getProduct(original.id) })
    }
}

