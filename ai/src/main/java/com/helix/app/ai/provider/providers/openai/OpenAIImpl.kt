package com.helix.app.ai.provider.providers.openai

import kotlinx.coroutines.flow.Flow
import com.helix.app.ai.provider.ProviderSetting
import com.helix.app.ai.provider.TextGenerationResult
import com.helix.app.ai.provider.TextGenerationParams
import com.helix.app.ai.ui.StreamChunk
import com.helix.app.ai.ui.UIMessage

interface OpenAIImpl {
    suspend fun generateText(
        providerSetting: ProviderSetting.OpenAI,
        messages: List<UIMessage>,
        params: TextGenerationParams,
    ): TextGenerationResult

    suspend fun streamText(
        providerSetting: ProviderSetting.OpenAI,
        messages: List<UIMessage>,
        params: TextGenerationParams,
    ): Flow<StreamChunk>
}
