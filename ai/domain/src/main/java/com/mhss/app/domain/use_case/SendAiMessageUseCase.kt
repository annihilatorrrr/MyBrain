package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.repository.AiRepository
import com.mhss.app.network.NetworkResult
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class SendAiMessageUseCase(private val aiRepository: AiRepository) {
    suspend operator fun invoke(messages: List<AiMessage>): NetworkResult<AiMessage> {
        return try {
            aiRepository.sendMessage(messages)
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError(e.message)
        }
    }
}