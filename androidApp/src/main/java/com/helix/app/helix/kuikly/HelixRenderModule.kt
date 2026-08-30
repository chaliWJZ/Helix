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
import com.helix.app.data.datastore.SettingsStore
import com.helix.app.ai.provider.ModelType
import com.helix.app.ai.provider.Provider
import com.helix.app.ai.provider.ProviderManager
import com.helix.app.ai.provider.ProviderSetting
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
                    val result = JSONObject().apply {
                        put("id", p.id.toString())
                        put("name", p.name)
                        put("enabled", p.enabled)
                        put("type", providerTypeName(p))
                        put("baseUrl", baseUrl)
                        put("apiKey", apiKey)
                        put("apiKeyMasked", if (apiKey.length > 8) apiKey.take(4) + "****" + apiKey.takeLast(4) else if (apiKey.isNotEmpty()) "****" else "")
                        put("models", JSONArray().apply {
                            p.models.forEach { m ->
                                put(JSONObject().apply {
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
                val name = json?.optString("name") ?: ""
                val baseUrl = json?.optString("baseUrl")
                val apiKey = json?.optString("apiKey")
                koinGet<AppScope>().launch {
                    try {
                        var updated = false
                        koinGet<SettingsStore>().update { s ->
                            s.copy(providers = s.providers.map { p ->
                                if (p.id.toString() == id) {
                                    updated = true
                                    applyProviderEdit(p, name, baseUrl, apiKey)
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

    /** 应用编辑（名称 + 凭证，按子类 copy） */
    private fun applyProviderEdit(
        p: ProviderSetting,
        name: String,
        baseUrl: String?,
        apiKey: String?
    ): ProviderSetting {
        val renamed = if (name.isNotEmpty()) p.copyProvider(name = name) else p
        return when (renamed) {
            is ProviderSetting.OpenAI -> renamed.copy(
                baseUrl = baseUrl ?: renamed.baseUrl,
                apiKey = apiKey ?: renamed.apiKey
            )
            is ProviderSetting.Google -> renamed.copy(
                baseUrl = baseUrl ?: renamed.baseUrl,
                apiKey = apiKey ?: renamed.apiKey
            )
            is ProviderSetting.Claude -> renamed.copy(
                baseUrl = baseUrl ?: renamed.baseUrl,
                apiKey = apiKey ?: renamed.apiKey
            )
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
        private const val MAX_URL_LENGTH = 2048
        /** 内部约定 scheme：跳 Compose 版对比 */
        const val COMPOSE_DEBUG_URL = "helix://compose-debug"
    }
}
