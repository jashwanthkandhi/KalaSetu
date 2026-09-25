package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.AppLanguage
import com.example.core.model.CraftCategory
import com.example.core.model.ListingStatus
import com.example.core.model.Product
import com.example.ui.viewmodel.KalaSetuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KalaSetu", appName)
  }

  @Test
  fun `viewModel language and online toggle test`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KalaSetuViewModel(app, backgroundSync = false)

    // Language change
    viewModel.setLanguage(AppLanguage.HINDI)
    assertEquals(AppLanguage.HINDI, viewModel.currentLanguage.value)

    viewModel.setLanguage(AppLanguage.TELUGU)
    assertEquals(AppLanguage.TELUGU, viewModel.currentLanguage.value)

    // Connectivity is observed from Android, never toggled by the user.
  }

  @Test
  fun `viewModel draft creation and edit test`() = runTest(testDispatcher) {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KalaSetuViewModel(app, backgroundSync = false)

    val sampleDraft = Product(
      id = "test-123",
      title = "Terracotta Water Pitcher",
      description = "Handmade clay water pitcher keeping water naturally cool",
      category = CraftCategory.POTTERY,
      suggestedPrice = 450.0,
      finalPrice = 450.0,
      tags = listOf("pottery", "handmade", "terracotta"),
      status = ListingStatus.DRAFT
    )

    viewModel.setDraftForReview(sampleDraft)
    assertNotNull(viewModel.currentDraft.value)
    assertEquals("Terracotta Water Pitcher", viewModel.currentDraft.value?.title)

    // Update title
    viewModel.updateDraftTitle("Artisan Handcrafted Pot")
    assertEquals("Artisan Handcrafted Pot", viewModel.currentDraft.value?.title)

    // Update category
    viewModel.updateDraftCategory(CraftCategory.HOME_DECOR)
    assertEquals(CraftCategory.HOME_DECOR, viewModel.currentDraft.value?.category)

    // Update price
    viewModel.updateDraftPrice(550.0)
    assertEquals(550.0, viewModel.currentDraft.value?.finalPrice ?: 0.0, 0.01)

    // Add tag
    viewModel.addDraftTag("kitchen")
    assertTrue(viewModel.currentDraft.value?.tags?.contains("kitchen") == true)

    // Remove tag
    viewModel.removeDraftTag("kitchen")
    assertFalse(viewModel.currentDraft.value?.tags?.contains("kitchen") == true)

    // Clear form
    viewModel.clearCaptureForm()
    assertEquals(null, viewModel.capturedImageUri.value)
    assertEquals(false, viewModel.isRecording.value)
  }
}
