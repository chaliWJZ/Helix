package com.helix.app.data.ai

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.helix.app.data.model.AssistantMemory
import com.helix.app.utils.JsonInstantPretty

internal fun buildMemoryPrompt(memories: List<AssistantMemory>) =
    buildString {
        appendLine()
        append("**Memories**")
        appendLine()
        append("These are memories stored via the memory_tool that you can reference in future conversations.")
        appendLine()
        val json = buildJsonArray {
            memories.forEach { memory ->
                add(buildJsonObject {
                    put("id", memory.id)
                    put("content", memory.content)
                })
            }
        }
        append(JsonInstantPretty.encodeToString(json))
        appendLine()
    }
