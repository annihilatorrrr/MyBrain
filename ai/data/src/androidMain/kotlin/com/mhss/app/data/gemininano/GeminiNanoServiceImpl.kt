package com.mhss.app.data.gemininano

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.PromptPrefix
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig
import com.mhss.app.domain.gemininano.GeminiNanoMode
import com.mhss.app.domain.gemininano.GeminiNanoService
import com.mhss.app.domain.gemininano.GeminiNanoStatus
import com.mhss.app.domain.model.AiMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.Closeable

@Single(binds = [GeminiNanoService::class])
class GeminiNanoServiceImpl(
    @Named("applicationScope") private val applicationScope: CoroutineScope
) : GeminiNanoService, Closeable {

    private val _status = MutableStateFlow<GeminiNanoStatus>(GeminiNanoStatus.Checking)
    override val status: StateFlow<GeminiNanoStatus> = _status.asStateFlow()

    private var model: GenerativeModel? = null
    private var currentMode: GeminiNanoMode? = null
    private var currentReleaseStage: Int? = null
    private var downloadJob: Job? = null
    private var detectedReleaseStage: Int = ModelReleaseStage.PREVIEW
    private var downloadingMode: GeminiNanoMode? = null
    private var downloadingReleaseStage: Int? = null

    override suspend fun refreshStatus(mode: GeminiNanoMode) {
        if (downloadJob?.isActive == true && downloadingMode == mode) return
        if (downloadJob?.isActive == true) {
            downloadJob?.cancel()
            downloadingMode = null
            downloadingReleaseStage = null
        }
        _status.value = GeminiNanoStatus.Checking
        var releaseStage = ModelReleaseStage.PREVIEW
        var status = checkAvailability(mode, releaseStage)

        if (status is GeminiNanoStatus.Unsupported || status is GeminiNanoStatus.Error) {
            // try stable release if preview is not available
            val stableStatus = checkAvailability(mode, ModelReleaseStage.STABLE)
            if (stableStatus !is GeminiNanoStatus.Unsupported && stableStatus !is GeminiNanoStatus.Error) {
                status = stableStatus
                releaseStage = ModelReleaseStage.STABLE
            }
        }

        detectedReleaseStage = releaseStage
        _status.value = status
    }

    private suspend fun checkAvailability(
        mode: GeminiNanoMode,
        releaseStage: Int
    ): GeminiNanoStatus {
        return runCatching {
            val status = getOrInitModel(mode, releaseStage).checkStatus()
            when (status) {
                FeatureStatus.AVAILABLE -> {
                    GeminiNanoStatus.Ready
                }
                FeatureStatus.DOWNLOADABLE -> GeminiNanoStatus.Downloadable()
                FeatureStatus.DOWNLOADING -> {
                    startDownload(mode, releaseStage)
                    GeminiNanoStatus.Downloadable(isDownloading = true)
                }
                FeatureStatus.UNAVAILABLE -> GeminiNanoStatus.Unsupported()
                else -> GeminiNanoStatus.Unsupported()
            }
        }.getOrElse { e ->
            GeminiNanoStatus.Unsupported(e.message)
        }
    }

    override fun download(mode: GeminiNanoMode) {
        startDownload(mode, detectedReleaseStage)
    }

    override suspend fun sendMessage(
        messages: List<AiMessage>,
        systemMessage: String,
        mode: GeminiNanoMode
    ): String {
        val model = getOrInitModel(mode, detectedReleaseStage)
        val request = generateContentRequest(TextPart(toGeminiNanoPrompt(messages))) {
            temperature = 0.65f
            topK = 20
            promptPrefix = PromptPrefix(systemMessage)
        }

        return model.generateContent(request).candidates.firstOrNull()?.text ?: ""
    }

    override fun sendPrompt(
        prompt: String,
        mode: GeminiNanoMode
    ): Flow<String> = flow {
        val model = getOrInitModel(mode, detectedReleaseStage)
        val request = generateContentRequest(TextPart(prompt)) {
            temperature = 0.65f
            topK = 20
        }
        model.generateContentStream(request).collect { response ->
            response.candidates.firstOrNull()?.text?.let { chunk ->
                emit(chunk)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun startDownload(mode: GeminiNanoMode, releaseStage: Int) {
        if (downloadJob?.isActive == true && downloadingMode == mode && downloadingReleaseStage == releaseStage) return
        downloadJob?.cancel()
        downloadingMode = mode
        downloadingReleaseStage = releaseStage
        downloadJob = applicationScope.launch {
            _status.value = GeminiNanoStatus.Downloadable(isDownloading = true)
            var totalBytes = 0L
            getOrInitModel(mode, releaseStage).download()
                .catch { e ->
                    _status.value = GeminiNanoStatus.Error(e.message ?: "Download failed")
                    downloadingMode = null
                    downloadingReleaseStage = null
                }
                .collect { downloadStatus ->
                    when (downloadStatus) {
                        is DownloadStatus.DownloadStarted -> {
                            totalBytes = downloadStatus.bytesToDownload
                            _status.value = GeminiNanoStatus.Downloadable(
                                progress = 0.01f,
                                isDownloading = true
                            )
                        }
                        is DownloadStatus.DownloadProgress -> {
                            val progress = if (totalBytes > 0) {
                                downloadStatus.totalBytesDownloaded.toFloat() / totalBytes
                            } else {
                                0f
                            }
                            _status.value = GeminiNanoStatus.Downloadable(
                                progress = progress.coerceIn(0f, 1f),
                                isDownloading = true
                            )
                        }
                        is DownloadStatus.DownloadFailed -> {
                            _status.value = GeminiNanoStatus.Error(
                                downloadStatus.e.message ?: "Download failed"
                            )
                            downloadingMode = null
                            downloadingReleaseStage = null
                        }
                        is DownloadStatus.DownloadCompleted -> {
                            _status.value = GeminiNanoStatus.Ready
                            downloadingMode = null
                            downloadingReleaseStage = null
                        }
                    }
                }
        }
    }

    private fun getOrInitModel(
        mode: GeminiNanoMode,
        releaseStage: Int
    ): GenerativeModel {
        if (model != null && currentMode == mode && currentReleaseStage == releaseStage) return model!!
        model?.close()
        currentMode = mode
        currentReleaseStage = releaseStage
        return Generation.getClient(
            generationConfig {
                modelConfig = modelConfig {
                    this.releaseStage = releaseStage
                    this.preference = mode.toModelPreference()
                }
            }
        ).also {
            model = it
        }
    }

    override suspend fun warmup(mode: GeminiNanoMode) {
        if (_status.value == GeminiNanoStatus.Checking) {
            refreshStatus(mode)
        }
        runCatching { getOrInitModel(mode, detectedReleaseStage).warmup() }
    }

    override fun close() {
        downloadJob?.cancel()
        downloadingMode = null
        downloadingReleaseStage = null
        model?.close()
        model = null
    }
}

private fun GeminiNanoMode.toModelPreference(): Int {
    return when (this) {
        GeminiNanoMode.Fast -> ModelPreference.FAST
        GeminiNanoMode.Full -> ModelPreference.FULL
    }
}

private fun toGeminiNanoPrompt(messages: List<AiMessage>): String = buildString {
    messages.forEach { message ->
        when (message) {
            is AiMessage.UserMessage -> {
                append("user: ")
                append(message.content)
                append(message.attachmentsText)
                append("\n")
            }
            is AiMessage.AssistantMessage -> {
                append("model: ")
                append(message.content)
                append("\n")
            }
            is AiMessage.ToolCall -> {
                append("tool: ")
                append(message.resultRawContent)
                append("\n")
            }
        }
    }
    append("model: ")
}
