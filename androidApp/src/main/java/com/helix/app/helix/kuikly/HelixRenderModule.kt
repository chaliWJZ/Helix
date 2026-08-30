package com.helix.app.helix.kuikly

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.helix.app.AppScope
import com.helix.app.common.android.LogEntry
import com.helix.app.common.android.Logging
import com.helix.app.data.datastore.RECOMMENDED_PROVIDERS
import com.helix.app.data.datastore.SettingsStore
import com.helix.app.data.ai.mcp.McpServerConfig
import com.helix.app.data.ai.mcp.serverUrl
import com.helix.app.ai.provider.ModelType
import com.helix.app.ai.provider.Provider
import com.helix.app.ai.provider.ProviderManager
import com.helix.app.ai.provider.ProviderSetting
import com.helix.app.search.SearchServiceOptions
import com.helix.app.service.WebServerService
import com.helix.app.web.WebServerManager
import me.rerere.asr.ASRProviderSetting
import me.rerere.tts.provider.TTSProviderSetting
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.uuid.Uuid

/**
 * Helix 原生侧 Kuikly Module
 *
 * 对应 kuikly-ui 模块中的 HelixBridgeModule（commonMain 侧）。
 * moduleName 必须与 HelixBridgeModule.MODULE_NAME 一致（"HelixBridgeModule"）。
 *
 * Kuikly 页面调用 bridge().toast("xx") → callNative("toast", json)
 * → 本类 call("toast", paramsJson, callback) 分发处理。
 */
class HelixRenderModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        val json = params?.let { runCatching { JSONObject(it) }.getOrNull() }
        Log.i(TAG, "🔗 [Module-Call] method=$method params=${params?.take(200)}")
        return when (method) {
            METHOD_TOAST -> {
                context?.let { Toast.makeText(it, json?.optString("message") ?: "", Toast.LENGTH_SHORT).show() }
                null
            }

            METHOD_OPEN_URL, METHOD_OPEN_WEBVIEW -> {
                val url = json?.optString("url") ?: ""
                when {
                    // 内部 scheme：跳 Compose 版对比
                    url == COMPOSE_DEBUG_URL -> {
                        val intent = Intent(context, com.helix.app.RouteActivity::class.java)
                        intent.putExtra("force_compose", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context?.startActivity(intent)
                    }
                    // 安全加固：仅允许 http/https scheme
                    isSafeUrl(url) -> {
                        runCatching {
                            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context?.startActivity(viewIntent)
                        }
                    }
                    else -> {
                        context?.let { Toast.makeText(it, "不支持的链接类型", Toast.LENGTH_SHORT).show() }
                    }
                }
                null
            }

            METHOD_SHARE -> {
                val text = json?.optString("text") ?: ""
                runCatching {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        // context 可能是 Application 级 context（非 Activity），
                        // 缺少 FLAG_ACTIVITY_NEW_TASK 时 startActivity 会抛
                        // AndroidRuntimeException 并被 runCatching 静默吞掉，
                        // 表现为点击分享无任何反应（测试发现的真实 bug）。
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context?.startActivity(Intent.createChooser(intent, "分享").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }.onFailure { e ->
                    Log.e(TAG, "⚠ [Error] shareText 调起分享面板失败: ${e.message}")
                    context?.let { Toast.makeText(it, "分享失败", Toast.LENGTH_SHORT).show() }
                }
                null
            }

            METHOD_GET_APP_INFO -> {
                JSONObject().apply {
                    put("versionName", "1.0.0")
                    put("versionCode", 1)
                    put("packageName", context?.packageName)
                }
            }

            METHOD_GET_SETTING -> JSONObject().apply { put("value", "") }
            METHOD_SET_SETTING -> true

            // ==================== 日志（真实数据：Logging 单例） ====================
            // ==================== 模型设置（真实数据：SettingsStore） ====================
            METHOD_GET_SETTINGS_SUMMARY -> {
                val settings = koinGet<SettingsStore>().settingsFlow.value
                fun findModelName(id: Uuid): String =
                    settings.providers.asSequence()
                        .flatMap { p -> p.models.map { m -> p.name to m } }
                        .firstOrNull { it.second.id == id }
                        ?.let { (provider, m) -> "${m.displayName.ifEmpty { m.modelId }}（$provider）" }
                        ?: "未知模型"
                val result = JSONObject().apply {
                    put("slots", JSONObject().apply {
                        fun slot(key: String, id: Uuid?) {
                            put(key, JSONObject().apply {
                                if (id != null) {
                                    put("id", id.toString())
                                    put("name", findModelName(id))
                                }
                            })
                        }
                        slot("chat", settings.chatModelId)
                        slot("fast", settings.fastModelId)
                        slot("title", settings.titleModelId)
                        slot("suggestion", settings.suggestionModelId)
                        slot("translate", settings.translateModeId)
                        slot("ocr", settings.ocrModelId)
                        slot("compress", settings.compressModelId)
                    })
                    put("enableSuggestion", settings.enableSuggestion)
                    put("models", JSONArray().apply {
                        settings.providers.filter { it.enabled }.forEach { p ->
                            p.models.filter { it.type == ModelType.CHAT }.forEach { m ->
                                put(JSONObject().apply {
                                    put("id", m.id.toString())
                                    put("name", m.displayName.ifEmpty { m.modelId })
                                    put("provider", p.name)
                                })
                            }
                        }
                    })
                }
                Log.i(TAG, "✅ [Module-Done] getSettingsSummary providers=${settings.providers.size} models=${result.optJSONArray("models")?.length()}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_MODEL_SLOT -> {
                val slot = json?.optString("slot") ?: ""
                val modelIdStr = json?.optString("modelId")
                val modelId = if (modelIdStr.isNullOrEmpty() || modelIdStr == "null") null
                else runCatching { Uuid.parse(modelIdStr) }.getOrNull()
                if (modelId == null && slot !in setOf("title", "suggestion")) {
                    Log.w(TAG, "⚠ [Module-Warn] setModelSlot $slot 不允许置空")
                    callback?.invoke(JSONObject().apply { put("error", "slot_not_nullable") })
                    null
                } else {
                    koinGet<AppScope>().launch {
                        try {
                            koinGet<SettingsStore>().update { s ->
                                when (slot) {
                                    "chat" -> s.copy(chatModelId = modelId ?: s.chatModelId)
                                    "fast" -> s.copy(fastModelId = modelId ?: s.fastModelId)
                                    "title" -> s.copy(titleModelId = modelId)
                                    "suggestion" -> s.copy(suggestionModelId = modelId)
                                    "translate" -> s.copy(translateModeId = modelId ?: s.translateModeId)
                                    "ocr" -> s.copy(ocrModelId = modelId ?: s.ocrModelId)
                                    "compress" -> s.copy(compressModelId = modelId ?: s.compressModelId)
                                    else -> s
                                }
                            }
                            Log.i(TAG, "✅ [Module-Done] setModelSlot $slot → $modelIdStr")
                            callback?.invoke(JSONObject().apply { put("ok", true) })
                        } catch (e: Exception) {
                            Log.e(TAG, "⚠ [Error] setModelSlot $slot: ${e.message}")
                            callback?.invoke(JSONObject().apply { put("error", e.message) })
                        }
                    }
                    null
                }
            }

            METHOD_SET_ENABLE_SUGGESTION -> {
                val enabled = json?.optBoolean("enabled") ?: false
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { it.copy(enableSuggestion = enabled) }
                        Log.i(TAG, "✅ [Module-Done] setEnableSuggestion → $enabled")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setEnableSuggestion: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== 供应商（真实数据：SettingsStore.providers） ====================
            METHOD_GET_PROVIDERS -> {
                val providers = koinGet<SettingsStore>().settingsFlow.value.providers
                val result = JSONArray().apply {
                    providers.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id.toString())
                            put("name", p.name)
                            put("enabled", p.enabled)
                            put("modelCount", p.models.size)
                            put("type", providerTypeName(p))
                        })
                    }
                }
                Log.i(TAG, "✅ [Module-Done] getProviders count=${providers.size}")
                callback?.invoke(JSONObject().apply { put("providers", result) })
                null
            }

            METHOD_GET_PROVIDER_DETAIL -> {
                val id = json?.optString("id") ?: ""
                val p = koinGet<SettingsStore>().settingsFlow.value.providers.firstOrNull { it.id.toString() == id }
                if (p == null) {
                    Log.w(TAG, "⚠ [Module-Warn] getProviderDetail 未找到 id=$id")
                    callback?.invoke(JSONObject().apply { put("error", "provider_not_found") })
                } else {
                    val (baseUrl, apiKey) = providerCredentials(p)
                    val defUrl = defaultBaseUrlOf(p)
                    val result = JSONObject().apply {
                        put("id", p.id.toString())
                        put("name", p.name)
                        put("enabled", p.enabled)
                        put("type", providerTypeName(p))
                        put("baseUrl", baseUrl)
                        put("apiKey", apiKey)
                        put("builtIn", p.builtIn)
                        put("isDefaultBaseUrl", baseUrl == defUrl)
                        put("defaultBaseUrl", defUrl)
                        when (p) {
                            is ProviderSetting.OpenAI -> {
                                put("chatCompletionsPath", p.chatCompletionsPath)
                                put("useResponseApi", p.useResponseApi)
                                put("includeHistoryReasoning", p.includeHistoryReasoning)
                                put("balanceEnabled", p.balanceOption.enabled)
                                put("balanceApiPath", p.balanceOption.apiPath)
                                put("balanceResultPath", p.balanceOption.resultPath)
                            }
                            is ProviderSetting.Claude -> {
                                put("promptCaching", p.promptCaching)
                                put("promptCacheTtl", p.promptCacheTtl.name)
                            }
                            is ProviderSetting.Google -> {
                                put("vertexAI", p.vertexAI)
                            }
                        }
                        put("models", JSONArray().apply {
                            p.models.forEach { m ->
                                put(JSONObject().apply {
                                    put("uid", m.id.toString())
                                    put("modelId", m.modelId)
                                    put("displayName", m.displayName)
                                    put("type", m.type.name)
                                })
                            }
                        })
                    }
                    Log.i(TAG, "✅ [Module-Done] getProviderDetail ${p.name} models=${p.models.size}")
                    callback?.invoke(result)
                }
                null
            }

            METHOD_UPDATE_PROVIDER -> {
                val id = json?.optString("id") ?: ""
                val edit = parseProviderEdit(json ?: JSONObject())
                koinGet<AppScope>().launch {
                    try {
                        var updated = false
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == id) {
                                    updated = true
                                    applyProviderEdit(p, edit)
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] updateProvider id=$id found=$updated")
                        callback?.invoke(JSONObject().apply { put("ok", updated) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] updateProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 从 API 拉取可选模型列表（listModels）
            METHOD_FETCH_PROVIDER_MODELS -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    val p = koinGet<SettingsStore>().settingsFlow.value.providers.firstOrNull { it.id.toString() == id }
                    if (p == null) {
                        callback?.invoke(JSONObject().apply { put("error", "provider_not_found") })
                        return@launch
                    }
                    try {
                        val manager = koinGet<ProviderManager>()
                        @Suppress("UNCHECKED_CAST")
                        val provider = manager.getProviderByType(p) as Provider<ProviderSetting>
                        val models = provider.listModels(p).sortedBy { it.modelId }
                        val result = JSONArray().apply {
                            models.forEach { m ->
                                put(JSONObject().apply {
                                    put("modelId", m.modelId)
                                    put("displayName", m.displayName)
                                    put("type", m.type.name)
                                })
                            }
                        }
                        Log.i(TAG, "✅ [Module-Done] fetchProviderModels ${p.name} 拉到 ${models.size} 个")
                        callback?.invoke(JSONObject().apply { put("models", result) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] fetchProviderModels: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message ?: "unknown") })
                    }
                }
                null
            }

            // 手动添加模型（或从拉取列表添加）
            METHOD_ADD_PROVIDER_MODEL -> {
                val providerId = json?.optString("providerId") ?: ""
                val modelId = json?.optString("modelId") ?: ""
                val displayName = json?.optString("displayName") ?: ""
                val type = json?.optString("type") ?: "CHAT"
                koinGet<AppScope>().launch {
                    try {
                        val modelType = runCatching { ModelType.valueOf(type) }.getOrDefault(ModelType.CHAT)
                        val model = com.helix.app.ai.provider.Model(
                            modelId = modelId,
                            displayName = displayName.ifEmpty { modelId },
                            type = modelType
                        )
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map {
                                if (it.id.toString() == providerId) it.addModel(model) else it
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] addProviderModel $modelId → $providerId")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] addProviderModel: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 编辑模型（modelId/displayName/type）
            METHOD_UPDATE_PROVIDER_MODEL -> {
                val providerId = json?.optString("providerId") ?: ""
                val modelUid = json?.optString("modelUid") ?: ""
                val modelId = json?.optString("modelId") ?: ""
                val displayName = json?.optString("displayName") ?: ""
                val type = json?.optString("type") ?: "CHAT"
                koinGet<AppScope>().launch {
                    try {
                        val modelType = runCatching { ModelType.valueOf(type) }.getOrDefault(ModelType.CHAT)
                        var updated = false
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == providerId) {
                                    val target = p.models.firstOrNull { it.id.toString() == modelUid }
                                    if (target != null) {
                                        updated = true
                                        p.editModel(
                                            target.copy(
                                                modelId = modelId,
                                                displayName = displayName,
                                                type = modelType
                                            )
                                        )
                                    } else p
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] updateProviderModel uid=$modelUid found=$updated")
                        callback?.invoke(JSONObject().apply { put("ok", updated) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] updateProviderModel: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 删除模型
            METHOD_DELETE_PROVIDER_MODEL -> {
                val providerId = json?.optString("providerId") ?: ""
                val modelUid = json?.optString("modelUid") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == providerId) {
                                    val target = p.models.firstOrNull { it.id.toString() == modelUid }
                                    if (target != null) p.delModel(target) else p
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] deleteProviderModel uid=$modelUid")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] deleteProviderModel: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 模型排序（上移/下移：from→to）
            METHOD_MOVE_PROVIDER_MODEL -> {
                val providerId = json?.optString("providerId") ?: ""
                val from = json?.optInt("from", -1) ?: -1
                val to = json?.optInt("to", -1) ?: -1
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == providerId && from in p.models.indices && to in p.models.indices) {
                                    p.moveMove(from, to)
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] moveProviderModel $from→$to")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] moveProviderModel: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 重置 BaseURL 为默认
            METHOD_RESET_PROVIDER_BASE_URL -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map {
                                if (it.id.toString() == id) it.resetBaseUrl() else it
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] resetProviderBaseUrl id=$id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] resetProviderBaseUrl: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 分享供应商配置（返回 JSON 字符串，Kuikly 侧走 shareText）
            METHOD_SHARE_PROVIDER_CONFIG -> {
                val id = json?.optString("id") ?: ""
                val p = koinGet<SettingsStore>().settingsFlow.value.providers.firstOrNull { it.id.toString() == id }
                if (p == null) {
                    callback?.invoke(JSONObject().apply { put("error", "provider_not_found") })
                } else {
                    try {
                        val jsonStr = com.helix.app.utils.JsonInstant.encodeToString(ProviderSetting.serializer(), p)
                        Log.i(TAG, "✅ [Module-Done] shareProviderConfig ${p.name} len=${jsonStr.length}")
                        callback?.invoke(JSONObject().apply { put("config", jsonStr) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] shareProviderConfig: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 拉取余额（OpenAI + balanceOption.enabled）
            METHOD_GET_PROVIDER_BALANCE -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    val p = koinGet<SettingsStore>().settingsFlow.value.providers.firstOrNull { it.id.toString() == id }
                    if (p == null || p !is ProviderSetting.OpenAI || !p.balanceOption.enabled) {
                        callback?.invoke(JSONObject().apply { put("error", "balance_disabled") })
                        return@launch
                    }
                    try {
                        val manager = koinGet<ProviderManager>()
                        @Suppress("UNCHECKED_CAST")
                        val provider = manager.getProviderByType(p) as Provider<ProviderSetting>
                        val balance = provider.getBalance(p)
                        Log.i(TAG, "✅ [Module-Done] getProviderBalance ${p.name} → $balance")
                        callback?.invoke(JSONObject().apply { put("balance", balance) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] getProviderBalance: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message ?: "unknown") })
                    }
                }
                null
            }

            // 切换供应商类型（保留 name/apiKey，baseUrl 转换为目标默认）
            METHOD_SWITCH_PROVIDER_TYPE -> {
                val id = json?.optString("id") ?: ""
                val newType = json?.optString("newType") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        var switched = false
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == id && providerTypeName(p) != newType) {
                                    switched = true
                                    switchProviderType(p, newType)
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] switchProviderType id=$id → $newType switched=$switched")
                        callback?.invoke(JSONObject().apply { put("ok", switched) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] switchProviderType: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_SET_PROVIDER_ENABLED -> {
                val id = json?.optString("id") ?: ""
                val enabled = json?.optBoolean("enabled") ?: false
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map {
                                if (it.id.toString() == id) it.copyProvider(enabled = enabled) else it
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] setProviderEnabled id=$id → $enabled")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setProviderEnabled: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_DELETE_PROVIDER -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.filterNot { it.id.toString() == id })
                        }
                        Log.i(TAG, "✅ [Module-Done] deleteProvider id=$id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] deleteProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_TEST_PROVIDER -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    val p = koinGet<SettingsStore>().settingsFlow.value.providers.firstOrNull { it.id.toString() == id }
                    if (p == null) {
                        callback?.invoke(JSONObject().apply { put("ok", false); put("error", "provider_not_found") })
                        return@launch
                    }
                    try {
                        val manager = koinGet<ProviderManager>()
                        @Suppress("UNCHECKED_CAST")
                        val provider = manager.getProviderByType(p) as Provider<ProviderSetting>
                        val models = provider.listModels(p)
                        Log.i(TAG, "✅ [Module-Done] testProvider ${p.name} 拉到 ${models.size} 个模型")
                        callback?.invoke(JSONObject().apply {
                            put("ok", true)
                            put("modelCount", models.size)
                        })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] testProvider ${p.name}: ${e.message}")
                        callback?.invoke(JSONObject().apply {
                            put("ok", false)
                            put("error", e.message ?: "unknown")
                        })
                    }
                }
                null
            }

            // ==================== 搜索服务（真实数据：Settings.searchServices） ====================
            METHOD_GET_SEARCH_SERVICES -> {
                val s = koinGet<SettingsStore>().settingsFlow.value
                val result = JSONObject().apply {
                    put("selected", s.searchServiceSelected)
                    put("services", JSONArray().apply {
                        s.searchServices.forEach { svc ->
                            put(JSONObject().apply {
                                put("id", svc.id.toString())
                                put("name", svc.displayName)
                                put("type", SearchServiceOptions.TYPES[svc::class] ?: "unknown")
                                put("hasApiKey", searchApiKeyOf(svc)?.isNotEmpty() == true)
                            })
                        }
                    })
                }
                Log.i(TAG, "✅ [Module-Done] getSearchServices count=${s.searchServices.size}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_SEARCH_SELECTED -> {
                val index = json?.optInt("index") ?: 0
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { it.copy(searchServiceSelected = index) }
                        Log.i(TAG, "✅ [Module-Done] setSearchSelected → $index")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setSearchSelected: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== TTS（真实数据：Settings.ttsProviders） ====================
            METHOD_GET_TTS_PROVIDERS -> {
                val s = koinGet<SettingsStore>().settingsFlow.value
                val result = JSONObject().apply {
                    put("providers", JSONArray().apply {
                        s.ttsProviders.forEach { p ->
                            put(JSONObject().apply {
                                put("id", p.id.toString())
                                put("name", p.name.ifEmpty { "系统 TTS" })
                                put("type", ttsTypeName(p))
                                put("isDefault", p.id == s.selectedTTSProviderId)
                            })
                        }
                    })
                }
                Log.i(TAG, "✅ [Module-Done] getTtsProviders count=${s.ttsProviders.size}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_DEFAULT_TTS -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        val uuid = runCatching { Uuid.parse(id) }.getOrNull()
                        if (uuid == null) {
                            callback?.invoke(JSONObject().apply { put("error", "bad_uuid") })
                            return@launch
                        }
                        koinGet<SettingsStore>().update { it.copy(selectedTTSProviderId = uuid) }
                        Log.i(TAG, "✅ [Module-Done] setDefaultTts → $id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setDefaultTts: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== MCP（真实数据：Settings.mcpServers） ====================
            METHOD_GET_MCP_SERVERS -> {
                val servers = koinGet<SettingsStore>().settingsFlow.value.mcpServers
                val result = JSONObject().apply {
                    put("servers", JSONArray().apply {
                        servers.forEach { m ->
                            put(JSONObject().apply {
                                put("id", m.id.toString())
                                put("name", m.commonOptions.name.ifEmpty { m.serverUrl })
                                put("url", m.serverUrl)
                                put("type", if (m is McpServerConfig.SseTransportServer) "sse" else "streamable_http")
                                put("enable", m.commonOptions.enable)
                            })
                        }
                    })
                }
                Log.i(TAG, "✅ [Module-Done] getMcpServers count=${servers.size}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_MCP_ENABLED -> {
                val id = json?.optString("id") ?: ""
                val enable = json?.optBoolean("enable") ?: false
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(mcpServers = s.mcpServers.map {
                                if (it.id.toString() == id)
                                    it.clone(commonOptions = it.commonOptions.copy(enable = enable))
                                else it
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] setMcpEnabled id=$id → $enable")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setMcpEnabled: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_DELETE_MCP_SERVER -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(mcpServers = s.mcpServers.filterNot { it.id.toString() == id })
                        }
                        Log.i(TAG, "✅ [Module-Done] deleteMcpServer id=$id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] deleteMcpServer: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== Web 服务器（真实数据：Settings + WebServerManager） ====================
            METHOD_GET_WEB_SERVER_STATE -> {
                val s = koinGet<SettingsStore>().settingsFlow.value
                val ws = koinGet<WebServerManager>().state.value
                val result = JSONObject().apply {
                    put("enabled", s.webServerEnabled)
                    put("running", ws.isRunning)
                    put("port", ws.port)
                    put("localhostOnly", s.webServerLocalhostOnly)
                    put("jwtEnabled", s.webServerJwtEnabled)
                    put("hasPassword", s.webServerAccessPassword.isNotEmpty())
                    put("address", ws.address ?: "")
                }
                Log.i(TAG, "✅ [Module-Done] getWebServerState running=${ws.isRunning}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_WEB_SERVER_ENABLED -> {
                val enabled = json?.optBoolean("enabled") ?: false
                koinGet<AppScope>().launch {
                    try {
                        val store = koinGet<SettingsStore>()
                        store.update { it.copy(webServerEnabled = enabled) }
                        val s = store.settingsFlow.value
                        if (enabled) {
                            val intent = Intent(context, WebServerService::class.java).apply {
                                action = WebServerService.ACTION_START
                                putExtra(WebServerService.EXTRA_PORT, s.webServerPort)
                                putExtra(WebServerService.EXTRA_LOCALHOST_ONLY, s.webServerLocalhostOnly)
                            }
                            context?.startService(intent)
                        } else {
                            val intent = Intent(context, WebServerService::class.java).apply {
                                action = WebServerService.ACTION_STOP
                            }
                            context?.startService(intent)
                        }
                        Log.i(TAG, "✅ [Module-Done] setWebServerEnabled → $enabled")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setWebServerEnabled: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_SET_WEB_SERVER_CONFIG -> {
                val port = json?.optInt("port", -1) ?: -1
                val password = json?.optString("password")
                val localhostOnly = json?.optBoolean("localhostOnly")
                val jwtEnabled = json?.optBoolean("jwtEnabled")
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(
                                webServerPort = if (port > 0) port else s.webServerPort,
                                webServerAccessPassword = password ?: s.webServerAccessPassword,
                                webServerLocalhostOnly = localhostOnly ?: s.webServerLocalhostOnly,
                                webServerJwtEnabled = jwtEnabled ?: s.webServerJwtEnabled
                            )
                        }
                        Log.i(TAG, "✅ [Module-Done] setWebServerConfig port=$port")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setWebServerConfig: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== 供应商列表扩展（批次B） ====================
            METHOD_GET_RECOMMENDED_PROVIDERS -> {
                val result = JSONArray().apply {
                    RECOMMENDED_PROVIDERS.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id.toString())
                            put("name", p.name)
                            put("type", providerTypeName(p))
                            put("baseUrl", providerCredentials(p).first)
                            put("added", false) // 由 Kuikly 侧对照已有列表标记
                        })
                    }
                }
                Log.i(TAG, "✅ [Module-Done] getRecommendedProviders count=${RECOMMENDED_PROVIDERS.size}")
                callback?.invoke(JSONObject().apply { put("recommended", result) })
                null
            }

            // 一键添加推荐供应商
            METHOD_ADD_RECOMMENDED_PROVIDER -> {
                val recId = json?.optString("recId") ?: ""
                val rec = RECOMMENDED_PROVIDERS.firstOrNull { it.id.toString() == recId }
                if (rec == null) {
                    callback?.invoke(JSONObject().apply { put("error", "not_found") })
                    null
                } else {
                    koinGet<AppScope>().launch {
                        try {
                            koinGet<SettingsStore>().update { s ->
                                if (s.providers.any { it.id == rec.id }) s
                                else s.copy(providers = s.providers + rec)
                            }
                            Log.i(TAG, "✅ [Module-Done] addRecommendedProvider ${rec.name}")
                            callback?.invoke(JSONObject().apply { put("ok", true) })
                        } catch (e: Exception) {
                            Log.e(TAG, "⚠ [Error] addRecommendedProvider: ${e.message}")
                            callback?.invoke(JSONObject().apply { put("error", e.message) })
                        }
                    }
                    null
                }
            }

            // 自定义添加供应商
            METHOD_ADD_CUSTOM_PROVIDER -> {
                val name = json?.optString("name") ?: ""
                val type = json?.optString("type") ?: "openai"
                val baseUrl = json?.optString("baseUrl") ?: ""
                val apiKey = json?.optString("apiKey") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        val p = when (type) {
                            "openai" -> ProviderSetting.OpenAI(
                                name = name.ifEmpty { "OpenAI" },
                                baseUrl = baseUrl.ifEmpty { ProviderSetting.OpenAI().baseUrl },
                                apiKey = apiKey
                            )
                            "google" -> ProviderSetting.Google(
                                name = name.ifEmpty { "Google" },
                                baseUrl = baseUrl.ifEmpty { ProviderSetting.Google().baseUrl },
                                apiKey = apiKey
                            )
                            "claude" -> ProviderSetting.Claude(
                                name = name.ifEmpty { "Claude" },
                                baseUrl = baseUrl.ifEmpty { ProviderSetting.Claude().baseUrl },
                                apiKey = apiKey
                            )
                            else -> null
                        }
                        if (p == null) {
                            callback?.invoke(JSONObject().apply { put("error", "bad_type") })
                            return@launch
                        }
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers + p)
                        }
                        Log.i(TAG, "✅ [Module-Done] addCustomProvider ${p.name} type=$type")
                        callback?.invoke(JSONObject().apply {
                            put("ok", true)
                            put("id", p.id.toString())
                        })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] addCustomProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // 供应商排序（上移/下移）
            METHOD_MOVE_PROVIDER -> {
                val from = json?.optInt("from", -1) ?: -1
                val to = json?.optInt("to", -1) ?: -1
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            if (from in s.providers.indices && to in s.providers.indices) {
                                s.copy(providers = s.providers.toMutableList().apply {
                                    val p = removeAt(from)
                                    add(to, p)
                                })
                            } else s
                        }
                        Log.i(TAG, "✅ [Module-Done] moveProvider $from→$to")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] moveProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            // ==================== TTS/ASR 扩展（批次C） ====================
            METHOD_GET_TTS_DETAIL -> {
                val id = json?.optString("id") ?: ""
                val s = koinGet<SettingsStore>().settingsFlow.value
                val p = s.ttsProviders.firstOrNull { it.id.toString() == id }
                if (p == null) {
                    callback?.invoke(JSONObject().apply { put("error", "not_found") })
                } else {
                    val result = JSONObject().apply {
                        put("id", p.id.toString())
                        put("name", p.name)
                        put("type", ttsTypeName(p))
                        put("isDefault", p.id == s.selectedTTSProviderId)
                        // 通用字段提取（各子类字段名一致：apiKey/baseUrl/model/voice）
                        put("apiKey", ttsField(p) { it.optString("apiKey") })
                        put("baseUrl", ttsField(p) { it.optString("baseUrl") })
                        put("model", ttsField(p) { it.optString("model") })
                        put("voice", ttsField(p) { it.optString("voice") })
                    }
                    Log.i(TAG, "✅ [Module-Done] getTtsDetail ${p.name}")
                    callback?.invoke(result)
                }
                null
            }

            METHOD_UPDATE_TTS_BASIC -> {
                val id = json?.optString("id") ?: ""
                val name = if (json?.optString("name")?.isNotEmpty() == true) json!!.optString("name") else null
                val apiKey = if (json?.has("apiKey") == true) json!!.optString("apiKey") else null
                val baseUrl = if (json?.has("baseUrl") == true) json!!.optString("baseUrl") else null
                koinGet<AppScope>().launch {
                    try {
                        var updated = false
                        koinGet<SettingsStore>().update { s ->
                            s.copy(ttsProviders = s.ttsProviders.map { p ->
                                if (p.id.toString() == id) {
                                    updated = true
                                    applyTtsEdit(p, name, apiKey, baseUrl)
                                } else p
                            })
                        }
                        Log.i(TAG, "✅ [Module-Done] updateTtsBasic id=$id found=$updated")
                        callback?.invoke(JSONObject().apply { put("ok", updated) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] updateTtsBasic: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_DELETE_TTS_PROVIDER -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(ttsProviders = s.ttsProviders.filterNot { it.id.toString() == id })
                        }
                        Log.i(TAG, "✅ [Module-Done] deleteTtsProvider id=$id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] deleteTtsProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_ADD_TTS_PROVIDER -> {
                val type = json?.optString("type") ?: "openai"
                koinGet<AppScope>().launch {
                    try {
                        val p = when (type) {
                            "openai" -> TTSProviderSetting.OpenAI()
                            "gemini" -> TTSProviderSetting.Gemini()
                            "system" -> TTSProviderSetting.SystemTTS()
                            "minimax" -> TTSProviderSetting.MiniMax()
                            "qwen" -> TTSProviderSetting.Qwen()
                            "groq" -> TTSProviderSetting.Groq()
                            "xai" -> TTSProviderSetting.XAI()
                            "mimo" -> TTSProviderSetting.MiMo()
                            "elevenlabs" -> TTSProviderSetting.ElevenLabs()
                            "step" -> TTSProviderSetting.Step()
                            "fish-audio" -> TTSProviderSetting.FishAudio()
                            else -> TTSProviderSetting.OpenAI()
                        }
                        koinGet<SettingsStore>().update { s ->
                            s.copy(ttsProviders = s.ttsProviders + p)
                        }
                        Log.i(TAG, "✅ [Module-Done] addTtsProvider $type")
                        callback?.invoke(JSONObject().apply {
                            put("ok", true)
                            put("id", p.id.toString())
                        })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] addTtsProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_SET_TTS_PLAYBACK_SPEED -> {
                val speed = json?.optDouble("speed", 1.0) ?: 1.0
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { it.copy(defaultTTSPlaybackSpeed = speed.toFloat().coerceIn(0.5f, 2.0f)) }
                        Log.i(TAG, "✅ [Module-Done] setTtsPlaybackSpeed → $speed")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setTtsPlaybackSpeed: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_GET_ASR_PROVIDERS -> {
                val s = koinGet<SettingsStore>().settingsFlow.value
                val result = JSONObject().apply {
                    put("selected", s.selectedASRProviderId?.toString() ?: "")
                    put("providers", JSONArray().apply {
                        s.asrProviders.forEach { p ->
                            put(JSONObject().apply {
                                put("id", p.id.toString())
                                put("name", p.name)
                                put("type", asrTypeName(p))
                                put("isDefault", p.id == s.selectedASRProviderId)
                            })
                        }
                    })
                }
                Log.i(TAG, "✅ [Module-Done] getASRProviders count=${s.asrProviders.size}")
                callback?.invoke(result)
                null
            }

            METHOD_SET_DEFAULT_ASR -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        val uuid = runCatching { Uuid.parse(id) }.getOrNull()
                        if (uuid == null) {
                            callback?.invoke(JSONObject().apply { put("error", "bad_uuid") })
                            return@launch
                        }
                        koinGet<SettingsStore>().update { it.copy(selectedASRProviderId = uuid) }
                        Log.i(TAG, "✅ [Module-Done] setDefaultASR → $id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] setDefaultASR: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_DELETE_ASR_PROVIDER -> {
                val id = json?.optString("id") ?: ""
                koinGet<AppScope>().launch {
                    try {
                        koinGet<SettingsStore>().update { s ->
                            s.copy(asrProviders = s.asrProviders.filterNot { it.id.toString() == id })
                        }
                        Log.i(TAG, "✅ [Module-Done] deleteAsrProvider id=$id")
                        callback?.invoke(JSONObject().apply { put("ok", true) })
                    } catch (e: Exception) {
                        Log.e(TAG, "⚠ [Error] deleteAsrProvider: ${e.message}")
                        callback?.invoke(JSONObject().apply { put("error", e.message) })
                    }
                }
                null
            }

            METHOD_GET_LOGS -> {
                val result = buildLogsJson()
                Log.i(TAG, "✅ [Module-Done] getLogs count=${result.optJSONArray("logs")?.length() ?: 0} enabled=${result.optBoolean("enabled")}")
                callback?.invoke(result)
                null
            }

            METHOD_GET_LOG_DETAIL -> {
                val id = json?.optString("id") ?: ""
                val entry = Logging.getRecentLogs().firstOrNull { it.id.toString() == id }
                if (entry is LogEntry.RequestLog) {
                    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    val result = JSONObject().apply {
                        put("id", entry.id.toString())
                        put("type", "request")
                        put("timeFull", fmt.format(Date(entry.timestamp)))
                        put("url", entry.url)
                        put("method", entry.method)
                        entry.responseCode?.let { put("responseCode", it) }
                        entry.durationMs?.let { put("durationMs", it) }
                        entry.error?.let { put("error", it) }
                        put("requestHeaders", JSONObject(entry.requestHeaders))
                        put("responseHeaders", JSONObject(entry.responseHeaders))
                        entry.requestBody?.let { put("requestBody", it) }
                    }
                    Log.i(TAG, "✅ [Module-Done] getLogDetail id=$id url=${entry.url.take(80)}")
                    callback?.invoke(result)
                } else {
                    Log.w(TAG, "⚠ [Module-Warn] getLogDetail 未找到 id=$id")
                    callback?.invoke(JSONObject().apply { put("error", "log_not_found") })
                }
                null
            }

            METHOD_CLEAR_LOGS -> {
                Logging.clear()
                Log.i(TAG, "✅ [Module-Done] clearLogs")
                callback?.invoke(JSONObject().apply { put("ok", true) })
                null
            }

            METHOD_SET_REQUEST_LOGGING -> {
                val enabled = json?.optBoolean("enabled") ?: false
                Logging.setRequestLoggingEnabled(enabled)
                Log.i(TAG, "✅ [Module-Done] setRequestLogging enabled=$enabled")
                callback?.invoke(JSONObject().apply {
                    put("ok", true)
                    put("enabled", enabled)
                })
                null
            }

            METHOD_COPY_TO_CLIPBOARD -> {
                val text = json?.optString("text") ?: ""
                try {
                    val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    cm?.setPrimaryClip(ClipData.newPlainText("HelixLog", text))
                    Log.i(TAG, "✅ [Module-Done] copyToClipboard length=${text.length}")
                    callback?.invoke(JSONObject().apply { put("ok", true) })
                } catch (e: Exception) {
                    Log.e(TAG, "⚠ [Error] copyToClipboard: ${e.message}")
                    callback?.invoke(JSONObject().apply { put("error", e.message) })
                }
                null
            }

            METHOD_READ_FILE, METHOD_WRITE_FILE, METHOD_LIST_FILES,
            METHOD_PICK_FILE, METHOD_TAKE_PHOTO, METHOD_PICK_IMAGE,
            METHOD_CHECK_UPDATE -> {
                callback?.invoke(JSONObject().apply { put("error", "not_implemented_yet") })
                null
            }

            else -> super.call(method, params, callback)
        }
    }

    /** Koin 全局容器取单例（Module 与 Koin 同进程） */
    private inline fun <reified T : Any> koinGet(): T = GlobalContext.get().get()

    /** 提取搜索服务 API Key（常用子类；无 key 的返回 null） */
    private fun searchApiKeyOf(svc: SearchServiceOptions): String? = when (svc) {
        is SearchServiceOptions.ZhipuOptions -> svc.apiKey
        is SearchServiceOptions.DoubaoOptions -> svc.apiKey
        is SearchServiceOptions.TavilyOptions -> svc.apiKey
        is SearchServiceOptions.ExaOptions -> svc.apiKey
        is SearchServiceOptions.LinkUpOptions -> svc.apiKey
        else -> null
    }

    /** TTS 子类类型名 */
    private fun ttsTypeName(p: TTSProviderSetting): String = when (p) {
        is TTSProviderSetting.OpenAI -> "openai"
        is TTSProviderSetting.Gemini -> "gemini"
        is TTSProviderSetting.SystemTTS -> "system"
        is TTSProviderSetting.MiniMax -> "minimax"
        is TTSProviderSetting.Qwen -> "qwen"
        is TTSProviderSetting.Groq -> "groq"
        is TTSProviderSetting.XAI -> "xai"
        is TTSProviderSetting.MiMo -> "mimo"
        is TTSProviderSetting.ElevenLabs -> "elevenlabs"
        is TTSProviderSetting.Step -> "step"
        is TTSProviderSetting.FishAudio -> "fish-audio"
    }

    /** TTS 通用字段反射提取（各子类字段同名，避免大量 when 分支） */
    private fun ttsField(p: TTSProviderSetting, getter: (org.json.JSONObject) -> String): String {
        return runCatching {
            val json = JSONObject(com.helix.app.utils.JsonInstant.encodeToString(TTSProviderSetting.serializer(), p))
            getter(json)
        }.getOrDefault("")
    }

    /** TTS 编辑（name/apiKey/baseUrl 通用三字段，按子类反射序列化后修改再反序列化） */
    private fun applyTtsEdit(p: TTSProviderSetting, name: String?, apiKey: String?, baseUrl: String?): TTSProviderSetting {
        if (name == null && apiKey == null && baseUrl == null) return p
        return runCatching {
            val json = JSONObject(com.helix.app.utils.JsonInstant.encodeToString(TTSProviderSetting.serializer(), p))
            name?.let { json.put("name", it) }
            apiKey?.let { json.put("apiKey", it) }
            baseUrl?.let { json.put("baseUrl", it) }
            com.helix.app.utils.JsonInstant.decodeFromString(TTSProviderSetting.serializer(), json.toString())
        }.getOrDefault(p)
    }

    /** ASR 子类类型名 */
    private fun asrTypeName(p: ASRProviderSetting): String = when (p) {
        is ASRProviderSetting.OpenAIRealtime -> "openai_realtime"
        is ASRProviderSetting.DashScope -> "dashscope"
        else -> p::class.simpleName?.lowercase() ?: "unknown"
    }

    /** 供应商子类类型名（@SerialName） */
    private fun providerTypeName(p: ProviderSetting): String = when (p) {
        is ProviderSetting.OpenAI -> "openai"
        is ProviderSetting.Google -> "google"
        is ProviderSetting.Claude -> "claude"
    }

    /** 提取供应商凭证（3 个子类均有 apiKey/baseUrl） */
    private fun providerCredentials(p: ProviderSetting): Pair<String, String> = when (p) {
        is ProviderSetting.OpenAI -> p.baseUrl to p.apiKey
        is ProviderSetting.Google -> p.baseUrl to p.apiKey
        is ProviderSetting.Claude -> p.baseUrl to p.apiKey
    }

    /** 各子类默认 baseUrl */
    private fun defaultBaseUrlOf(p: ProviderSetting): String = when (p) {
        is ProviderSetting.OpenAI -> ProviderSetting.OpenAI().baseUrl
        is ProviderSetting.Google -> ProviderSetting.Google().baseUrl
        is ProviderSetting.Claude -> ProviderSetting.Claude().baseUrl
    }

    /** 重置 baseUrl 为默认 */
    private fun ProviderSetting.resetBaseUrl(): ProviderSetting = when (this) {
        is ProviderSetting.OpenAI -> copy(baseUrl = defaultBaseUrlOf(this))
        is ProviderSetting.Google -> copy(baseUrl = defaultBaseUrlOf(this))
        is ProviderSetting.Claude -> copy(baseUrl = defaultBaseUrlOf(this))
    }

    /** 编辑参数（全部可选，null=不改） */
    private class ProviderEdit(
        val name: String? = null,
        val baseUrl: String? = null,
        val apiKey: String? = null,
        val chatCompletionsPath: String? = null,
        val useResponseApi: Boolean? = null,
        val includeHistoryReasoning: Boolean? = null,
        val promptCaching: Boolean? = null,
        val promptCacheTtl: String? = null,
        val vertexAI: Boolean? = null,
        val balanceEnabled: Boolean? = null,
        val balanceApiPath: String? = null,
        val balanceResultPath: String? = null
    )

    private fun parseProviderEdit(j: JSONObject): ProviderEdit = ProviderEdit(
        name = if (j.optString("name").isNotEmpty()) j.optString("name") else null,
        baseUrl = if (j.has("baseUrl")) j.optString("baseUrl") else null,
        apiKey = if (j.has("apiKey")) j.optString("apiKey") else null,
        chatCompletionsPath = if (j.has("chatCompletionsPath")) j.optString("chatCompletionsPath") else null,
        useResponseApi = if (j.has("useResponseApi")) j.optBoolean("useResponseApi") else null,
        includeHistoryReasoning = if (j.has("includeHistoryReasoning")) j.optBoolean("includeHistoryReasoning") else null,
        promptCaching = if (j.has("promptCaching")) j.optBoolean("promptCaching") else null,
        promptCacheTtl = if (j.has("promptCacheTtl")) j.optString("promptCacheTtl") else null,
        vertexAI = if (j.has("vertexAI")) j.optBoolean("vertexAI") else null,
        balanceEnabled = if (j.has("balanceEnabled")) j.optBoolean("balanceEnabled") else null,
        balanceApiPath = if (j.has("balanceApiPath")) j.optString("balanceApiPath") else null,
        balanceResultPath = if (j.has("balanceResultPath")) j.optString("balanceResultPath") else null
    )

    /** 应用编辑（全字段，按子类 copy，不适用字段忽略） */
    private fun applyProviderEdit(p: ProviderSetting, e: ProviderEdit): ProviderSetting {
        val renamed = e.name?.let { p.copyProvider(name = it) } ?: p
        return when (renamed) {
            is ProviderSetting.OpenAI -> renamed.copy(
                baseUrl = e.baseUrl ?: renamed.baseUrl,
                apiKey = e.apiKey ?: renamed.apiKey,
                chatCompletionsPath = e.chatCompletionsPath ?: renamed.chatCompletionsPath,
                useResponseApi = e.useResponseApi ?: renamed.useResponseApi,
                includeHistoryReasoning = e.includeHistoryReasoning ?: renamed.includeHistoryReasoning,
                balanceOption = renamed.balanceOption.copy(
                    enabled = e.balanceEnabled ?: renamed.balanceOption.enabled,
                    apiPath = e.balanceApiPath ?: renamed.balanceOption.apiPath,
                    resultPath = e.balanceResultPath ?: renamed.balanceOption.resultPath
                )
            )
            is ProviderSetting.Google -> renamed.copy(
                baseUrl = e.baseUrl ?: renamed.baseUrl,
                apiKey = e.apiKey ?: renamed.apiKey,
                vertexAI = e.vertexAI ?: renamed.vertexAI
            )
            is ProviderSetting.Claude -> renamed.copy(
                baseUrl = e.baseUrl ?: renamed.baseUrl,
                apiKey = e.apiKey ?: renamed.apiKey,
                promptCaching = e.promptCaching ?: renamed.promptCaching,
                promptCacheTtl = e.promptCacheTtl?.let {
                    runCatching { com.helix.app.ai.provider.ClaudePromptCacheTtl.valueOf(it) }.getOrDefault(renamed.promptCacheTtl)
                } ?: renamed.promptCacheTtl
            )
        }
    }

    /** 切换供应商类型（保留 name/apiKey，baseUrl 用目标类型默认） */
    private fun switchProviderType(p: ProviderSetting, newType: String): ProviderSetting {
        val name = p.name
        val apiKey = providerCredentials(p).second
        return when (newType) {
            "openai" -> ProviderSetting.OpenAI(name = name, apiKey = apiKey)
            "google" -> ProviderSetting.Google(name = name, apiKey = apiKey)
            "claude" -> ProviderSetting.Claude(name = name, apiKey = apiKey)
            else -> p
        }
    }

    /** URL 安全校验：仅允许 http/https */
    private fun isSafeUrl(url: String): Boolean {
        if (url.isEmpty() || url.length > MAX_URL_LENGTH) return false
        val scheme = runCatching { Uri.parse(url).scheme?.lowercase() }.getOrNull()
        return scheme == "http" || scheme == "https"
    }

    /** 组装日志列表 JSON（列表卡片字段裁剪，大 body 走 getLogDetail 按需拉取） */
    private fun buildLogsJson(): JSONObject {
        val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return JSONObject().apply {
            put("enabled", Logging.isRequestLoggingEnabled())
            put("logs", JSONArray().apply {
                Logging.getRecentLogs().forEach { entry ->
                    put(JSONObject().apply {
                        put("id", entry.id.toString())
                        put("type", if (entry is LogEntry.RequestLog) "request" else "text")
                        put("timeText", fmt.format(Date(entry.timestamp)))
                        put("tag", entry.tag)
                        when (entry) {
                            is LogEntry.TextLog -> put("message", entry.message)
                            is LogEntry.RequestLog -> {
                                put("url", entry.url)
                                put("method", entry.method)
                                entry.responseCode?.let { put("responseCode", it) }
                                entry.durationMs?.let { put("durationMs", it) }
                                entry.error?.let { put("error", it) }
                            }
                        }
                    })
                }
            })
        }
    }

    companion object {
        private const val TAG = "HelixKuikly"
        const val MODULE_NAME = "HelixBridgeModule"
        const val METHOD_TOAST = "toast"
        const val METHOD_OPEN_URL = "openUrl"
        const val METHOD_SHARE = "shareText"
        const val METHOD_OPEN_WEBVIEW = "openWebView"
        const val METHOD_READ_FILE = "readFile"
        const val METHOD_WRITE_FILE = "writeFile"
        const val METHOD_LIST_FILES = "listFiles"
        const val METHOD_PICK_FILE = "pickFile"
        const val METHOD_TAKE_PHOTO = "takePhoto"
        const val METHOD_PICK_IMAGE = "pickImage"
        const val METHOD_GET_APP_INFO = "getAppInfo"
        const val METHOD_CHECK_UPDATE = "checkUpdate"
        const val METHOD_GET_SETTING = "getSetting"
        const val METHOD_SET_SETTING = "setSetting"
        const val METHOD_GET_LOGS = "getLogs"
        const val METHOD_GET_LOG_DETAIL = "getLogDetail"
        const val METHOD_CLEAR_LOGS = "clearLogs"
        const val METHOD_SET_REQUEST_LOGGING = "setRequestLogging"
        const val METHOD_COPY_TO_CLIPBOARD = "copyToClipboard"
        const val METHOD_GET_SETTINGS_SUMMARY = "getSettingsSummary"
        const val METHOD_SET_MODEL_SLOT = "setModelSlot"
        const val METHOD_SET_ENABLE_SUGGESTION = "setEnableSuggestion"
        const val METHOD_GET_PROVIDERS = "getProviders"
        const val METHOD_GET_PROVIDER_DETAIL = "getProviderDetail"
        const val METHOD_UPDATE_PROVIDER = "updateProvider"
        const val METHOD_SET_PROVIDER_ENABLED = "setProviderEnabled"
        const val METHOD_DELETE_PROVIDER = "deleteProvider"
        const val METHOD_TEST_PROVIDER = "testProvider"
        const val METHOD_FETCH_PROVIDER_MODELS = "fetchProviderModels"
        const val METHOD_ADD_PROVIDER_MODEL = "addProviderModel"
        const val METHOD_UPDATE_PROVIDER_MODEL = "updateProviderModel"
        const val METHOD_DELETE_PROVIDER_MODEL = "deleteProviderModel"
        const val METHOD_MOVE_PROVIDER_MODEL = "moveProviderModel"
        const val METHOD_RESET_PROVIDER_BASE_URL = "resetProviderBaseUrl"
        const val METHOD_SHARE_PROVIDER_CONFIG = "shareProviderConfig"
        const val METHOD_GET_PROVIDER_BALANCE = "getProviderBalance"
        const val METHOD_SWITCH_PROVIDER_TYPE = "switchProviderType"
        const val METHOD_GET_RECOMMENDED_PROVIDERS = "getRecommendedProviders"
        const val METHOD_ADD_RECOMMENDED_PROVIDER = "addRecommendedProvider"
        const val METHOD_ADD_CUSTOM_PROVIDER = "addCustomProvider"
        const val METHOD_MOVE_PROVIDER = "moveProvider"
        const val METHOD_GET_TTS_DETAIL = "getTtsDetail"
        const val METHOD_UPDATE_TTS_BASIC = "updateTtsBasic"
        const val METHOD_DELETE_TTS_PROVIDER = "deleteTtsProvider"
        const val METHOD_ADD_TTS_PROVIDER = "addTtsProvider"
        const val METHOD_SET_TTS_PLAYBACK_SPEED = "setTtsPlaybackSpeed"
        const val METHOD_GET_ASR_PROVIDERS = "getASRProviders"
        const val METHOD_SET_DEFAULT_ASR = "setDefaultASR"
        const val METHOD_DELETE_ASR_PROVIDER = "deleteAsrProvider"
        const val METHOD_GET_SEARCH_SERVICES = "getSearchServices"
        const val METHOD_SET_SEARCH_SELECTED = "setSearchSelected"
        const val METHOD_GET_TTS_PROVIDERS = "getTtsProviders"
        const val METHOD_SET_DEFAULT_TTS = "setDefaultTts"
        const val METHOD_GET_MCP_SERVERS = "getMcpServers"
        const val METHOD_SET_MCP_ENABLED = "setMcpEnabled"
        const val METHOD_DELETE_MCP_SERVER = "deleteMcpServer"
        const val METHOD_GET_WEB_SERVER_STATE = "getWebServerState"
        const val METHOD_SET_WEB_SERVER_ENABLED = "setWebServerEnabled"
        const val METHOD_SET_WEB_SERVER_CONFIG = "setWebServerConfig"
        private const val MAX_URL_LENGTH = 2048
        /** 内部约定 scheme：跳 Compose 版对比 */
        const val COMPOSE_DEBUG_URL = "helix://compose-debug"
    }
}
