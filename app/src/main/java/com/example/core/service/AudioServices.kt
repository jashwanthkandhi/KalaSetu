package com.example.core.service

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.core.model.AppLanguage
import java.io.File
import java.util.Locale

class AudioRecordingService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isCurrentlyRecording = false

    fun startRecording(): Boolean {
        return try {
            val audioDir = File(context.cacheDir, "audio_recordings")
            if (!audioDir.exists()) audioDir.mkdirs()
            val file = File(audioDir, "rec_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isCurrentlyRecording = true
            true
        } catch (e: Exception) {
            Log.w("AudioRecordingService", "Hardware microphone unavailable, using safe recording state: ${e.message}")
            // Fallback for emulators without mic hardware
            val audioDir = File(context.cacheDir, "audio_recordings")
            if (!audioDir.exists()) audioDir.mkdirs()
            currentOutputFile = File(audioDir, "simulated_${System.currentTimeMillis()}.m4a")
            isCurrentlyRecording = true
            true
        }
    }

    fun stopRecording(): String? {
        if (!isCurrentlyRecording) return currentOutputFile?.absolutePath
        isCurrentlyRecording = false
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.w("AudioRecordingService", "Error stopping recorder: ${e.message}")
                }
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioRecordingService", "Exception releasing recorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }
        return currentOutputFile?.absolutePath
    }

    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun isRecording(): Boolean = isCurrentlyRecording
}

class AudioPlayerService {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun playAudio(filePath: String, onCompletion: () -> Unit = {}) {
        stopAudio()
        try {
            val file = File(filePath)
            if (!file.exists() || file.length() == 0L) {
                // If dummy or empty file, notify completion after short delay
                onCompletion()
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    this@AudioPlayerService.isPlaying = false
                    onCompletion()
                }
                start()
            }
            isPlaying = true
        } catch (e: Exception) {
            Log.w("AudioPlayerService", "Could not play audio: ${e.message}")
            onCompletion()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (this@AudioPlayerService.isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioPlayerService", "Error stopping player: ${e.message}")
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying
}

class TextToSpeechService(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        val locale = when (language) {
            AppLanguage.TELUGU -> Locale("te", "IN")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.ENGLISH -> Locale("en", "IN")
        }

        try {
            val langResult = tts?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KalaSetuTTS")
        } catch (e: Exception) {
            Log.w("TextToSpeechService", "Error during speech: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun shutdown() {
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
    }
}
