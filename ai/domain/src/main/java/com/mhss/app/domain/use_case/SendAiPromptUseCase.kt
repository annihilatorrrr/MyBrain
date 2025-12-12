package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AiRepository
import com.mhss.app.network.NetworkResult
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class SendAiPromptUseCase(private val aiRepository: AiRepository) {
    suspend operator fun invoke(prompt: String): NetworkResult<String> {
        return try {
            aiRepository.sendPrompt(prompt)
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError()
        }
    }
}