package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.*
import com.example.core.model.*
import com.example.core.network.NetworkApiService
import com.example.core.service.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.UUID

class KalaSetuViewModel @JvmOverloads constructor(application: Application, private val apiService: ApiService = NetworkApiService(application), private val backgroundSync: Boolean = true) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("kalasetu_prefs", Context.MODE_PRIVATE)
    private val repository = KalaSetuRepository(KalaSetuDatabase.getDatabase(application))
    private val sync = SyncEngine(repository, apiService)
    private val recordingService = AudioRecordingService(application)
    private val playerService = AudioPlayerService()
    private val ttsService = TextToSpeechService(application)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val profileAdapter = moshi.adapter(ArtisanProfile::class.java)
    private val preferencesAdapter = moshi.adapter(AppPreferences::class.java)
    private val connectivity = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val offlineQueue = repository.offlineQueue.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val currentLanguage = MutableStateFlow(AppLanguage.entries.find { it.code == prefs.getString("selected_language", "te") } ?: AppLanguage.TELUGU)
    val tutorialSeen = MutableStateFlow(prefs.getBoolean("tutorial_seen", false))
    val isOnline = MutableStateFlow(checkConnectivity())
    val profile = MutableStateFlow(runCatching { profileAdapter.fromJson(prefs.getString("profile", "{}")!!) }.getOrNull() ?: ArtisanProfile())
    val preferences = MutableStateFlow(runCatching { preferencesAdapter.fromJson(prefs.getString("preferences", "{}")!!) }.getOrNull() ?: AppPreferences())
    val isVoiceGuideEnabled = MutableStateFlow(prefs.getBoolean("voice_guide_enabled", false))
    val capturedImageUri = MutableStateFlow(prefs.getString("capture_photo", null))
    val recordedAudioPath = MutableStateFlow(prefs.getString("capture_audio", null))
    val isRecording = MutableStateFlow(false)
    val recordingDurationSeconds = MutableStateFlow(0)
    val isPlayingAudio = MutableStateFlow(false)
    val processingStage = MutableStateFlow(ProcessingStage.TRANSCRIBING)
    val isProcessing = MutableStateFlow(false)
    val processingError = MutableStateFlow<String?>(null)
    val currentDraft = MutableStateFlow<Product?>(null)
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    val discovery = MutableStateFlow<List<Product>>(emptyList())
    val discoveryLoading = MutableStateFlow(false)
    val discoveryError = MutableStateFlow<String?>(null)
    val discoveryHasMore = MutableStateFlow(true)
    val assistantProposal = MutableStateFlow<Product?>(null)
    val favorites = MutableStateFlow(prefs.getStringSet("favorites", emptySet())!!.toSet())
    private var captureId = prefs.getString("capture_id", null) ?: UUID.randomUUID().toString()
    private var recordingTimerJob: Job? = null
    private var voiceEditing = false
    private var voiceEditAudioPath: String? = null
    private var draftSaveJob: Job? = null
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { updateConnectivity() }
        override fun onLost(network: Network) { updateConnectivity() }
    }
    init {
        runCatching { connectivity.registerDefaultNetworkCallback(networkCallback) }
        viewModelScope.launch { repository.recoverInterrupted(); scheduleSync() }
    }
    private fun checkConnectivity(): Boolean = runCatching {
        connectivity.getNetworkCapabilities(connectivity.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }.getOrDefault(false)
    private fun updateConnectivity() {
        isOnline.value = checkConnectivity()
        if (isOnline.value) scheduleSync()
    }
    private fun scheduleSync() { if (backgroundSync) CatalogSyncWorker.schedule(getApplication()) }
    fun setLanguage(language: AppLanguage) { currentLanguage.value = language; prefs.edit().putString("selected_language", language.code).apply() }
    fun completeOnboarding() { tutorialSeen.value = true; prefs.edit().putBoolean("tutorial_seen", true).apply() }
    fun resetOnboarding() { tutorialSeen.value = false; prefs.edit().putBoolean("tutorial_seen", false).apply() }
    fun toggleVoiceGuide() { isVoiceGuideEnabled.value = !isVoiceGuideEnabled.value; prefs.edit().putBoolean("voice_guide_enabled", isVoiceGuideEnabled.value).apply() }
    fun speakGuidance(text: String) { if (isVoiceGuideEnabled.value) ttsService.speak(text, currentLanguage.value, preferences.value.speechSpeed) }
    fun updateProfile(value: ArtisanProfile) { profile.value = value; prefs.edit().putString("profile", profileAdapter.toJson(value)).apply() }
    fun updatePreferences(value: AppPreferences) {
        preferences.value = value
        prefs.edit().putString("preferences", preferencesAdapter.toJson(value))
            .putBoolean("notify_processing", value.processingNotifications).putBoolean("notify_sync", value.syncNotifications)
            .putBoolean("notify_error", value.errorNotifications).apply()
    }
    fun setProfilePhoto(uri: String) = action { updateProfile(profile.value.copy(photo = copyMedia(uri, "profile"))) }
    private suspend fun copyMedia(uri: String, prefix: String): String = withContext(Dispatchers.IO) {
        val directory = File(getApplication<Application>().filesDir, "media").apply { mkdirs() }
        val file = File(directory, "${prefix}_${UUID.randomUUID()}.jpg")
        getApplication<Application>().contentResolver.openInputStream(Uri.parse(uri)).use { input ->
            requireNotNull(input) { "Photo unavailable" }
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.toURI().toString()
    }
    fun setCapturedPhoto(uri: String) = action {
        capturedImageUri.value = copyMedia(uri, "product")
        persistCapture()
    }
    private suspend fun persistCapture() {
        prefs.edit().putString("capture_id", captureId).putString("capture_photo", capturedImageUri.value)
            .putString("capture_audio", recordedAudioPath.value).apply()
        if (capturedImageUri.value != null) {
            repository.saveProduct(Product(captureId, "", "", CraftCategory.OTHER, emptyList(),
                localImageUri = capturedImageUri.value, suggestedPrice = 0.0, finalPrice = 0.0,
                status = ListingStatus.DRAFT, languageCode = currentLanguage.value.code, captureAudioPath = recordedAudioPath.value))
        }
    }
    fun startRecording(forVoiceEdit: Boolean = false) {
        if (isRecording.value) return
        voiceEditing = forVoiceEdit
        if (!recordingService.startRecording()) { message.value = "Microphone unavailable. Allow microphone access and try again."; return }
        isRecording.value = true
        if (forVoiceEdit) voiceEditAudioPath = null else recordedAudioPath.value = null
        recordingDurationSeconds.value = 0
        recordingTimerJob = viewModelScope.launch {
            while (isRecording.value) {
                delay(1000); recordingDurationSeconds.value++
                if (recordingDurationSeconds.value >= 29) stopRecording()
            }
        }
    }
    fun startVoiceEditRecording() { startRecording(forVoiceEdit = true) }
    fun stopRecording() {
        if (!isRecording.value) return
        val path = recordingService.stopRecording()
        if (voiceEditing) voiceEditAudioPath = path else recordedAudioPath.value = path
        isRecording.value = false
        recordingTimerJob?.cancel()
        if (path == null) message.value = "Recording was too short or failed. Please try again."
        if (!voiceEditing) viewModelScope.launch { persistCapture() }
    }
    fun clearRecording() {
        playerService.stopAudio(); isPlayingAudio.value = false
        recordedAudioPath.value = null; recordingDurationSeconds.value = 0
        viewModelScope.launch { persistCapture() }
    }
    fun playRecordedAudio() { recordedAudioPath.value?.let { isPlayingAudio.value = true; playerService.playAudio(it) { isPlayingAudio.value = false } } }
    fun canGenerateListing() = capturedImageUri.value != null && recordedAudioPath.value != null && !isRecording.value
    fun triggerGenerateListing(onNavigateToProcessing: () -> Unit, onNavigateToCatalog: () -> Unit) {
        if (!canGenerateListing() || isProcessing.value) return
        viewModelScope.launch {
            val photo = capturedImageUri.value ?: return@launch
            val audio = recordedAudioPath.value ?: return@launch
            val product = Product(captureId, "", "", CraftCategory.OTHER, emptyList(), localImageUri = photo,
                suggestedPrice = 0.0, finalPrice = 0.0, status = ListingStatus.PENDING_UPLOAD,
                languageCode = currentLanguage.value.code, captureAudioPath = audio)
            repository.enqueue(product, OfflineQueueEntity(captureId, photo, audio, currentLanguage.value.code,
                product.createdAt, "queued", 0, null))
            if (isOnline.value) { onNavigateToProcessing(); executeProcessingPipeline() }
            else { scheduleSync(); clearCaptureForm(); onNavigateToCatalog() }
        }
    }
    fun executeProcessingPipeline() {
        if (isProcessing.value) return
        isProcessing.value = true; currentDraft.value = null; processingError.value = null
        processingStage.value = ProcessingStage.TRANSCRIBING
        viewModelScope.launch {
            try {
                currentDraft.value = sync.process(captureId) { stage -> processingStage.value = stage }
                clearCaptureForm()
                processingStage.value = ProcessingStage.COMPLETED
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                processingError.value = "Could not create this listing. Your photo and recording are saved. Retry or open the sync center."
                processingStage.value = ProcessingStage.FAILED
            } finally { isProcessing.value = false }
        }
    }
    fun setDraftForReview(product: Product) {
        currentDraft.value = product
        assistantProposal.value = null
    }
    fun resumeCapture(product: Product) {
        captureId = product.id; capturedImageUri.value = product.localImageUri
        recordedAudioPath.value = product.captureAudioPath
        currentLanguage.value = AppLanguage.entries.find { it.code == product.languageCode } ?: currentLanguage.value
    }
    private fun edit(transform: (Product) -> Product) {
        val draft = currentDraft.value ?: return
        val next = transform(draft).copy(updatedAt = maxOf(System.currentTimeMillis(), draft.updatedAt + 1), status = ListingStatus.DRAFT)
        currentDraft.value = next
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch { repository.saveProduct(next) }
    }
    fun updateDraftTitle(value: String) = edit { it.copy(title = value) }
    fun updateDraftTags(value: List<String>) = edit { it.copy(tags = value.distinct()) }
    fun updateDraftDescription(value: String) = edit { it.copy(description = value) }
    fun updateDraftCategory(value: CraftCategory) = edit { it.copy(category = value) }
    fun updateDraftPrice(value: Double) = edit { it.copy(finalPrice = value) }
    fun addDraftTag(value: String) = edit { it.copy(tags = (it.tags + value.trim()).filter(String::isNotBlank).distinct()) }
    fun removeDraftTag(value: String) = edit { it.copy(tags = it.tags - value) }
    fun updateDraftAttributes(value: Map<String, String>) = edit { it.copy(attributes = value) }
    fun saveDraft() = action { currentDraft.value?.let { repository.saveProduct(it) }; message.value = "Draft saved on this device." }
    fun confirmListing(onComplete: () -> Unit) {
        val draft = currentDraft.value ?: return
        if (busy.value || draft.qualityIssues().isNotEmpty()) { message.value = draft.qualityIssues().firstOrNull(); return }
        busy.value = true
        viewModelScope.launch {
            draftSaveJob?.join()
            try {
                sync.confirm(draft.copy(publicProfile = profile.value))
                clearCaptureForm(); currentDraft.value = null; onComplete()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                message.value = "Cloud save failed. Your reviewed listing is pending sync and will retry."
                scheduleSync()
            } finally { busy.value = false }
        }
    }
    fun syncOfflineQueue() = action {
        repository.queueItems().forEach { repository.enqueueOfflineItem(it.copy(status = "queued", retryCount = 0)) }
        scheduleSync(); message.value = "Sync scheduled. Connect to the internet to continue."
    }
    fun retryProduct(product: Product) = action {
        repository.queueItems().find { it.id == product.id }?.let { repository.enqueueOfflineItem(it.copy(status = "queued", retryCount = 0)) }
        scheduleSync()
    }
    fun refreshCatalog() = action {
        apiService.catalog().forEach { remote ->
            val local = repository.getProduct(remote.id)
            if (local == null || (local.status in listOf(ListingStatus.SAVED, ListingStatus.ARCHIVED) && remote.updatedAt > local.updatedAt))
                repository.saveProduct(remote.copy(localImageUri = local?.localImageUri, isFavorite = local?.isFavorite ?: false))
        }
    }
    fun loadDiscovery(more: Boolean = false) {
        if (discoveryLoading.value) return
        discoveryLoading.value = true; discoveryError.value = null
        viewModelScope.launch {
            try {
                val page = apiService.discover(if (more) discovery.value.size else 0)
                discovery.value = (if (more) discovery.value + page else page).distinctBy { it.id }
                discoveryHasMore.value = page.size == 50
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                discoveryError.value = "Marketplace is unavailable. Check your connection and retry."
            } finally { discoveryLoading.value = false }
        }
    }
    fun deleteProduct(id: String, onDeleted: (() -> Unit)? = null) = action {
        val product = repository.getProduct(id) ?: return@action
        require(product.status != ListingStatus.UPLOADING && product.status != ListingStatus.PENDING_CONFIRM) { "Wait for sync to finish before deleting." }
        if (product.remoteSaved) apiService.delete(product)
        repository.deleteProduct(id); onDeleted?.invoke()
    }
    fun archiveProduct(product: Product) = action {
        val archived = product.copy(status = ListingStatus.ARCHIVED, updatedAt = System.currentTimeMillis())
        if (product.remoteSaved) apiService.confirmListing(archived)
        repository.saveProduct(archived)
    }
    fun duplicateProduct(product: Product, onReady: () -> Unit) = action {
        val duplicate = product.copy(id = UUID.randomUUID().toString(), status = ListingStatus.DRAFT,
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), remoteSaved = false, lastError = null)
        setDraftForReview(duplicate); repository.saveProduct(duplicate); onReady()
    }
    fun toggleFavorite(id: String) {
        favorites.value = if (id in favorites.value) favorites.value - id else favorites.value + id
        prefs.edit().putStringSet("favorites", favorites.value).apply()
    }
    fun requestAssistant(instruction: String) = action {
        currentDraft.value?.let { assistantProposal.value = apiService.assist(it, instruction, currentLanguage.value) }
    }
    fun requestVoiceEdit() = action {
        val path = voiceEditAudioPath ?: return@action
        val command = apiService.voiceEdit(path, currentLanguage.value)
        currentDraft.value?.let { assistantProposal.value = apiService.assist(it, command, currentLanguage.value) }
    }
    fun applyAssistantProposal() { assistantProposal.value?.let { proposal -> edit { proposal } }; assistantProposal.value = null }
    fun readListingAloud() = action {
        val p = currentDraft.value ?: return@action
        val lang = AppLanguage.entries.find { it.code == preferences.value.ttsLanguage } ?: currentLanguage.value
        val text = "${p.title}. ${p.description}. ₹${p.finalPrice}"
        try {
            val bytes = apiService.readAloud(text, lang)
            val file = File(getApplication<Application>().cacheDir, "listing_voice.audio")
            withContext(Dispatchers.IO) { file.writeBytes(bytes) }
            playerService.playAudio(file.absolutePath, preferences.value.speechSpeed)
        } catch (_: Exception) { ttsService.speak(text, lang, preferences.value.speechSpeed) }
    }
    fun clearCaptureForm() {
        voiceEditing = false
        capturedImageUri.value = null; recordedAudioPath.value = null; isRecording.value = false
        recordingDurationSeconds.value = 0; captureId = UUID.randomUUID().toString()
        prefs.edit().remove("capture_photo").remove("capture_audio").remove("capture_id").apply()
    }
    private fun action(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { block() } catch (e: Exception) {
                if (e is CancellationException) throw e
                message.value = "This action could not finish. Your work is kept. Check your connection and try again."
            } finally { busy.value = false }
        }
    }
    override fun onCleared() {
        runCatching { connectivity.unregisterNetworkCallback(networkCallback) }
        if (isRecording.value) recordingService.stopRecording()
        playerService.stopAudio(); ttsService.shutdown(); recordingTimerJob?.cancel()
        super.onCleared()
    }
}
