package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.KalaSetuDatabase
import com.example.core.data.KalaSetuRepository
import com.example.core.data.OfflineQueueEntity
import com.example.core.model.AppLanguage
import com.example.core.model.CraftCategory
import com.example.core.model.ListingStatus
import com.example.core.model.ProcessResponse
import com.example.core.model.ProcessingStage
import com.example.core.model.Product
import com.example.core.network.NetworkApiService
import com.example.core.service.ApiService
import com.example.core.service.AudioPlayerService
import com.example.core.service.AudioRecordingService
import com.example.core.service.MockApiService
import com.example.core.service.TextToSpeechService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class KalaSetuViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("kalasetu_prefs", Context.MODE_PRIVATE)

    private val repository: KalaSetuRepository
    private val apiService: ApiService = NetworkApiService(application)
    private val recordingService = AudioRecordingService(application)
    private val playerService = AudioPlayerService()
    private val ttsService = TextToSpeechService(application)

    init {
        val db = KalaSetuDatabase.getDatabase(application)
        repository = KalaSetuRepository(db.productDao(), db.offlineQueueDao())

        // Preload initial authentic craft listings
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    // 1. Language & Onboarding State
    private val _currentLanguage = MutableStateFlow(loadSavedLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _tutorialSeen = MutableStateFlow(prefs.getBoolean("tutorial_seen", false))
    val tutorialSeen: StateFlow<Boolean> = _tutorialSeen.asStateFlow()

    // 2. Connectivity State (real network + demo toggle)
    private val _isOnline = MutableStateFlow(checkInitialConnectivity(application))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // 3. Catalog & Offline Queue State
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineQueue: StateFlow<List<OfflineQueueEntity>> = repository.offlineQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Capture Screen State
    private val _capturedImageUri = MutableStateFlow<String?>(null)
    val capturedImageUri: StateFlow<String?> = _capturedImageUri.asStateFlow()

    private val _recordedAudioPath = MutableStateFlow<String?>(null)
    val recordedAudioPath: StateFlow<String?> = _recordedAudioPath.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private var recordingTimerJob: Job? = null

    // 5. Processing Screen State
    private val _processingStage = MutableStateFlow(ProcessingStage.TRANSCRIBING)
    val processingStage: StateFlow<ProcessingStage> = _processingStage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingError = MutableStateFlow<String?>(null)
    val processingError: StateFlow<String?> = _processingError.asStateFlow()

    // 6. Review Screen State
    private val _currentDraft = MutableStateFlow<Product?>(null)
    val currentDraft: StateFlow<Product?> = _currentDraft.asStateFlow()

    // 7. Extra Artisan Features: Voice Guide & Profile
    private val _isVoiceGuideEnabled = MutableStateFlow(prefs.getBoolean("voice_guide_enabled", false))
    val isVoiceGuideEnabled: StateFlow<Boolean> = _isVoiceGuideEnabled.asStateFlow()

    private val _artisanName = MutableStateFlow(prefs.getString("artisan_name", "Lakshmi Devi") ?: "Lakshmi Devi")
    val artisanName: StateFlow<String> = _artisanName.asStateFlow()

    private val _artisanCraftSpecialty = MutableStateFlow(prefs.getString("artisan_specialty", "Master Potter & Clay Sculptor") ?: "Master Potter & Clay Sculptor")
    val artisanCraftSpecialty: StateFlow<String> = _artisanCraftSpecialty.asStateFlow()

    private val _artisanLocation = MutableStateFlow(prefs.getString("artisan_location", "Pochampally, Telangana") ?: "Pochampally, Telangana")
    val artisanLocation: StateFlow<String> = _artisanLocation.asStateFlow()

    private val _artisanPhone = MutableStateFlow(prefs.getString("artisan_phone", "+91 98480 12345") ?: "+91 98480 12345")
    val artisanPhone: StateFlow<String> = _artisanPhone.asStateFlow()

    private fun loadSavedLanguage(): AppLanguage {
        val code = prefs.getString("selected_language", "te") ?: "te"
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.TELUGU
    }

    private fun checkInitialConnectivity(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return true // default optimistic
            val caps = cm.getNetworkCapabilities(network) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    // Actions
    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("selected_language", language.code).apply()
        if (_isVoiceGuideEnabled.value) {
            val msg = when (language) {
                AppLanguage.TELUGU -> "భాష తెలుగుకు మార్చబడింది."
                AppLanguage.HINDI -> "भाषा हिंदी में बदल दी गई है।"
                AppLanguage.ENGLISH -> "Language changed to English."
            }
            ttsService.speak(msg, language)
        }
    }

    fun toggleVoiceGuide() {
        val next = !_isVoiceGuideEnabled.value
        _isVoiceGuideEnabled.value = next
        prefs.edit().putBoolean("voice_guide_enabled", next).apply()
        val announcement = if (next) {
            when (_currentLanguage.value) {
                AppLanguage.TELUGU -> "వాయిస్ అసిస్టెంట్ గైడ్ ప్రారంభించబడింది."
                AppLanguage.HINDI -> "वॉइस गाइड चालू कर दिया गया है।"
                AppLanguage.ENGLISH -> "Voice guidance assistant enabled."
            }
        } else {
            when (_currentLanguage.value) {
                AppLanguage.TELUGU -> "వాయిస్ గైడ్ ఆపివేయబడింది."
                AppLanguage.HINDI -> "वॉइस गाइड बंद कर दिया गया है।"
                AppLanguage.ENGLISH -> "Voice guidance assistant disabled."
            }
        }
        ttsService.speak(announcement, _currentLanguage.value)
    }

    fun speakGuidance(text: String) {
        if (_isVoiceGuideEnabled.value) {
            ttsService.speak(text, _currentLanguage.value)
        }
    }

    fun updateArtisanProfile(name: String, specialty: String, location: String, phone: String) {
        _artisanName.value = name
        _artisanCraftSpecialty.value = specialty
        _artisanLocation.value = location
        _artisanPhone.value = phone
        prefs.edit()
            .putString("artisan_name", name)
            .putString("artisan_specialty", specialty)
            .putString("artisan_location", location)
            .putString("artisan_phone", phone)
            .apply()
    }

    fun deleteProduct(id: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            if (_isVoiceGuideEnabled.value) {
                val announcement = when (_currentLanguage.value) {
                    AppLanguage.TELUGU -> "ఉత్పత్తి తొలగించబడింది."
                    AppLanguage.HINDI -> "उत्पाद हटा दिया गया है।"
                    AppLanguage.ENGLISH -> "Product has been deleted."
                }
                ttsService.speak(announcement, _currentLanguage.value)
            }
            onDeleted?.invoke()
        }
    }

    fun completeOnboarding() {
        _tutorialSeen.value = true
        prefs.edit().putBoolean("tutorial_seen", true).apply()
    }

    fun resetOnboarding() {
        _tutorialSeen.value = false
        prefs.edit().putBoolean("tutorial_seen", false).apply()
    }

    fun toggleOnlineStatus() {
        val newState = !_isOnline.value
        _isOnline.value = newState
        if (newState) {
            syncOfflineQueue()
        }
    }

    fun setCapturedPhoto(uri: String) {
        _capturedImageUri.value = uri
    }

    fun startRecording() {
        if (_isRecording.value) return
        recordingService.startRecording()
        _isRecording.value = true
        _recordingDurationSeconds.value = 0

        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDurationSeconds.value += 1
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        val path = recordingService.stopRecording()
        _isRecording.value = false
        recordingTimerJob?.cancel()
        _recordedAudioPath.value = path ?: "simulated_audio.m4a"
    }

    fun clearRecording() {
        playerService.stopAudio()
        _isPlayingAudio.value = false
        _recordedAudioPath.value = null
        _recordingDurationSeconds.value = 0
    }

    fun playRecordedAudio() {
        val path = _recordedAudioPath.value ?: return
        _isPlayingAudio.value = true
        playerService.playAudio(path) {
            _isPlayingAudio.value = false
        }
    }

    fun canGenerateListing(): Boolean {
        return _capturedImageUri.value != null && _recordedAudioPath.value != null
    }

    fun triggerGenerateListing(
        onNavigateToProcessing: () -> Unit,
        onNavigateToCatalog: () -> Unit
    ) {
        val photo = _capturedImageUri.value ?: return
        val audio = _recordedAudioPath.value

        if (_isOnline.value) {
            // Online flow -> Processing screen
            onNavigateToProcessing()
            executeProcessingPipeline()
        } else {
            // Offline flow -> Queue to Room & add pending item to catalog
            viewModelScope.launch {
                val queueItem = OfflineQueueEntity(
                    id = UUID.randomUUID().toString(),
                    photoUri = photo,
                    audioPath = audio,
                    languageCode = _currentLanguage.value.code,
                    createdAt = System.currentTimeMillis(),
                    status = "queued",
                    retryCount = 0,
                    lastError = null
                )
                repository.enqueueOfflineItem(queueItem)

                // Add to catalog with Pending Upload status
                val pendingProduct = Product(
                    id = queueItem.id,
                    title = "Pending Handcrafted Listing",
                    description = "Captured offline. Will synchronize and generate AI listing once connected.",
                    category = CraftCategory.POTTERY,
                    tags = listOf("Offline", "Pending Upload"),
                    localImageUri = photo,
                    suggestedPrice = 850.0,
                    finalPrice = 850.0,
                    status = ListingStatus.PENDING_UPLOAD,
                    createdAt = System.currentTimeMillis()
                )
                repository.saveProduct(pendingProduct)

                // Reset capture form
                clearCaptureForm()
                onNavigateToCatalog()
            }
        }
    }

    fun executeProcessingPipeline() {
        val photo = _capturedImageUri.value ?: return
        val audio = _recordedAudioPath.value

        _isProcessing.value = true
        _processingError.value = null

        viewModelScope.launch {
            try {
                val response: ProcessResponse = apiService.processListing(
                    photoUri = photo,
                    audioPath = audio,
                    language = _currentLanguage.value,
                    onStageUpdate = { stage ->
                        _processingStage.value = stage
                    }
                )

                // Populate Draft for Review & Edit
                _currentDraft.value = Product(
                    id = response.requestId,
                    title = response.title,
                    description = response.description,
                    category = response.category,
                    tags = response.tags,
                    localImageUri = photo,
                    originalImageUrl = response.originalImageUrl,
                    imageUrl = response.enhancedImageUrl,
                    voiceTranscript = response.transcript,
                    suggestedPrice = response.suggestedPrice,
                    finalPrice = response.suggestedPrice,
                    status = ListingStatus.DRAFT,
                    createdAt = System.currentTimeMillis()
                )

                _isProcessing.value = false
            } catch (e: Exception) {
                _processingStage.value = ProcessingStage.FAILED
                _processingError.value = "Processing failed: ${e.message}"
                _isProcessing.value = false
            }
        }
    }

    fun setDraftForReview(product: Product) {
        _currentDraft.value = product
    }

    // Review & Edit updates
    fun updateDraftTitle(newTitle: String) {
        _currentDraft.value = _currentDraft.value?.copy(title = newTitle)
    }

    fun updateDraftDescription(newDesc: String) {
        _currentDraft.value = _currentDraft.value?.copy(description = newDesc)
    }

    fun updateDraftCategory(newCategory: CraftCategory) {
        _currentDraft.value = _currentDraft.value?.copy(category = newCategory)
    }

    fun updateDraftPrice(newPrice: Double) {
        _currentDraft.value = _currentDraft.value?.copy(finalPrice = newPrice)
    }

    fun addDraftTag(tag: String) {
        val current = _currentDraft.value ?: return
        if (tag.isNotBlank() && !current.tags.contains(tag)) {
            _currentDraft.value = current.copy(tags = current.tags + tag)
        }
    }

    fun removeDraftTag(tag: String) {
        val current = _currentDraft.value ?: return
        _currentDraft.value = current.copy(tags = current.tags.filter { it != tag })
    }

    fun readListingAloud() {
        val draft = _currentDraft.value ?: return
        val textToSpeak = "${draft.title}. ${draft.category.displayName}. ${draft.description}. Price: ${draft.finalPrice.toInt()} rupees."
        ttsService.speak(textToSpeak, _currentLanguage.value)
    }

    fun confirmListing(onComplete: () -> Unit) {
        val draft = _currentDraft.value ?: return
        viewModelScope.launch {
            try {
                val remoteId = apiService.confirmListing(draft)
                val confirmedProduct = draft.copy(id = remoteId, status = ListingStatus.SAVED)
                repository.saveProduct(confirmedProduct)
            } catch (e: Exception) {
                android.util.Log.w("KalaSetuViewModel", "Confirm remote sync warning: ${e.message}, saving locally.")
                val confirmedProduct = draft.copy(status = ListingStatus.SAVED)
                repository.saveProduct(confirmedProduct)
            }
            clearCaptureForm()
            _currentDraft.value = null
            onComplete()
        }
    }

    fun syncOfflineQueue() {
        viewModelScope.launch {
            val items = offlineQueue.value
            for (item in items) {
                try {
                    val lang = AppLanguage.entries.find { it.code == item.languageCode } ?: AppLanguage.TELUGU
                    val response = apiService.processListing(
                        photoUri = item.photoUri,
                        audioPath = item.audioPath,
                        language = lang,
                        onStageUpdate = {}
                    )
                    val confirmedProduct = Product(
                        id = response.requestId,
                        title = response.title,
                        description = response.description,
                        category = response.category,
                        tags = response.tags,
                        localImageUri = item.photoUri,
                        originalImageUrl = response.originalImageUrl,
                        imageUrl = response.enhancedImageUrl,
                        voiceTranscript = response.transcript,
                        suggestedPrice = response.suggestedPrice,
                        finalPrice = response.suggestedPrice,
                        status = ListingStatus.SAVED,
                        createdAt = System.currentTimeMillis()
                    )
                    apiService.confirmListing(confirmedProduct)
                    repository.saveProduct(confirmedProduct)
                    // Remove pending placeholder and queue item only on verified success
                    repository.deleteProduct(item.id)
                    repository.removeOfflineItem(item.id)
                } catch (e: Exception) {
                    android.util.Log.w("KalaSetuViewModel", "Failed to sync offline item ${item.id}: ${e.message}")
                }
            }
        }
    }

    fun clearCaptureForm() {
        _capturedImageUri.value = null
        _recordedAudioPath.value = null
        _isRecording.value = false
        _recordingDurationSeconds.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        playerService.stopAudio()
        ttsService.shutdown()
        recordingTimerJob?.cancel()
    }
}
