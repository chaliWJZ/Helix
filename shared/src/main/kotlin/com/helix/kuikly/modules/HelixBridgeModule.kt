package com.helix.kuikly.modules

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Helix 原生能力桥接 Module
 *
 * Kuikly 页面通过 acquireModule<HelixBridgeModule> 调用 Android 原生能力。
 * 宿主（app 模块）在 KuiklyEngine 初始化时注册方法处理器。
 *
 * 使用示例：
 * ```
 * acquireModule<HelixBridgeModule>(HelixBridgeModule.MODULE_NAME).toast("Hello")
 * ```
 */
class HelixBridgeModule : Module() {

    override fun moduleName(): String = MODULE_NAME

    // ==================== UI 能力 ====================

    /** 显示 Toast 提示 */
    fun toast(message: String) {
        callNative(METHOD_TOAST, JSONObject().apply { put("message", message) }, null)
    }

    /** 打开系统浏览器/外部 URL */
    fun openUrl(url: String) {
        callNative(METHOD_OPEN_URL, JSONObject().apply { put("url", url) }, null)
    }

    /** 分享文本到其他应用 */
    fun shareText(text: String) {
        callNative(METHOD_SHARE, JSONObject().apply { put("text", text) }, null)
    }

    // ==================== WebView ====================

    /** 打开原生 WebView 页面（全屏） */
    fun openWebView(url: String, title: String = "") {
        callNative(
            METHOD_OPEN_WEBVIEW,
            JSONObject().apply {
                put("url", url)
                put("title", title)
            },
            null
        )
    }

    // ==================== 文件系统 ====================

    /** 读取文本文件 */
    fun readFile(path: String, callback: CallbackFn?) {
        if (!isSafePath(path)) {
            callback?.invoke(JSONObject().apply { put("error", "illegal_path") })
            return
        }
        callNative(METHOD_READ_FILE, JSONObject().apply { put("path", path) }, callback)
    }

    /** 写入文本文件 */
    fun writeFile(path: String, content: String, callback: CallbackFn?) {
        if (!isSafePath(path)) {
            callback?.invoke(JSONObject().apply { put("error", "illegal_path") })
            return
        }
        callNative(
            METHOD_WRITE_FILE,
            JSONObject().apply {
                put("path", path)
                put("content", content)
            },
            callback
        )
    }

    /** 列出目录下的文件 */
    fun listFiles(dirPath: String, callback: CallbackFn?) {
        if (!isSafePath(dirPath)) {
            callback?.invoke(JSONObject().apply { put("error", "illegal_path") })
            return
        }
        callNative(METHOD_LIST_FILES, JSONObject().apply { put("dirPath", dirPath) }, callback)
    }

    /**
     * 文件路径合法性校验（修复 stbug-6G5qHbJbKp）
     *
     * 白名单式防御：拒绝空路径、超长路径、路径遍历（..）、绝对路径（/ 开头）、
     * 空字节注入、危险字符。原生侧文件操作真正实现时，此校验作为第一道防线。
     */
    private fun isSafePath(path: String): Boolean {
        if (path.isEmpty() || path.length > 2048) return false
        if (path.contains('\u0000')) return false  // NUL 字节注入
        if (path.contains("..")) return false       // 路径遍历
        if (path.startsWith("/")) return false      // 绝对路径（仅允许相对工作区路径）
        // 允许：字母数字、中划线、下划线、点、斜杠（相对路径分隔）、空格
        return path.all { c ->
            c.isLetterOrDigit() || c == '-' || c == '_' || c == '.' || c == '/' || c == ' '
        }
    }

    /** 选择本地文件（文件选择器） */
    fun pickFile(callback: CallbackFn?) {
        callNative(METHOD_PICK_FILE, null, callback)
    }

    // ==================== 相机/图片 ====================

    /** 拍照 */
    fun takePhoto(callback: CallbackFn?) {
        callNative(METHOD_TAKE_PHOTO, null, callback)
    }

    /** 从相册选图 */
    fun pickImage(callback: CallbackFn?) {
        callNative(METHOD_PICK_IMAGE, null, callback)
    }

    // ==================== 供应商（真实数据：SettingsStore.providers） ====================

    /** 供应商列表。返回：{providers:[{id,name,enabled,modelCount,type}]} */
    fun getProviders(callback: CallbackFn?) {
        callNative(METHOD_GET_PROVIDERS, null, callback)
    }

    /** 供应商详情。返回：{id,name,enabled,type,baseUrl,apiKey,apiKeyMasked,models:[{modelId,displayName,type}]} */
    fun getProviderDetail(id: String, callback: CallbackFn?) {
        callNative(METHOD_GET_PROVIDER_DETAIL, JSONObject().apply { put("id", id) }, callback)
    }

    /** 编辑供应商（name/baseUrl/apiKey 任一非空则更新；传空串=不更新） */
    fun updateProvider(id: String, name: String, baseUrl: String, apiKey: String, callback: CallbackFn?) {
        callNative(
            METHOD_UPDATE_PROVIDER,
            JSONObject().apply {
                put("id", id)
                put("name", name)
                put("baseUrl", baseUrl)
                put("apiKey", apiKey)
            },
            callback
        )
    }

    /** 启用/禁用供应商 */
    fun setProviderEnabled(id: String, enabled: Boolean, callback: CallbackFn?) {
        callNative(
            METHOD_SET_PROVIDER_ENABLED,
            JSONObject().apply {
                put("id", id)
                put("enabled", enabled)
            },
            callback
        )
    }

    /** 编辑供应商（原始参数版：调用方按需组 JSON 字段，字段见原生侧 ProviderEdit） */
    fun updateProviderRaw(params: JSONObject, callback: CallbackFn?) {
        callNative(METHOD_UPDATE_PROVIDER, params, callback)
    }

    /** 删除供应商 */
    fun deleteProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_PROVIDER, JSONObject().apply { put("id", id) }, callback)
    }

    /** 测试供应商连通性（原生 listModels）。返回：{ok, modelCount|error} */
    fun testProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_TEST_PROVIDER, JSONObject().apply { put("id", id) }, callback)
    }

    /** 从 API 拉取可选模型列表。返回：{models:[{modelId,displayName,type}]} */
    fun fetchProviderModels(id: String, callback: CallbackFn?) {
        callNative(METHOD_FETCH_PROVIDER_MODELS, JSONObject().apply { put("id", id) }, callback)
    }

    /** 添加模型（手动或从拉取列表） */
    fun addProviderModel(providerId: String, modelId: String, displayName: String, type: String, callback: CallbackFn?) {
        callNative(
            METHOD_ADD_PROVIDER_MODEL,
            JSONObject().apply {
                put("providerId", providerId)
                put("modelId", modelId)
                put("displayName", displayName)
                put("type", type)
            },
            callback
        )
    }

    /** 编辑模型（modelId/displayName/type） */
    fun updateProviderModel(providerId: String, modelUid: String, modelId: String, displayName: String, type: String, callback: CallbackFn?) {
        callNative(
            METHOD_UPDATE_PROVIDER_MODEL,
            JSONObject().apply {
                put("providerId", providerId)
                put("modelUid", modelUid)
                put("modelId", modelId)
                put("displayName", displayName)
                put("type", type)
            },
            callback
        )
    }

    /** 删除模型 */
    fun deleteProviderModel(providerId: String, modelUid: String, callback: CallbackFn?) {
        callNative(
            METHOD_DELETE_PROVIDER_MODEL,
            JSONObject().apply {
                put("providerId", providerId)
                put("modelUid", modelUid)
            },
            callback
        )
    }

    /** 模型排序（上移/下移） */
    fun moveProviderModel(providerId: String, from: Int, to: Int, callback: CallbackFn?) {
        callNative(
            METHOD_MOVE_PROVIDER_MODEL,
            JSONObject().apply {
                put("providerId", providerId)
                put("from", from)
                put("to", to)
            },
            callback
        )
    }

    /** 重置 BaseURL 为默认 */
    fun resetProviderBaseUrl(id: String, callback: CallbackFn?) {
        callNative(METHOD_RESET_PROVIDER_BASE_URL, JSONObject().apply { put("id", id) }, callback)
    }

    /** 分享供应商配置（返回 JSON 字符串） */
    fun shareProviderConfig(id: String, callback: CallbackFn?) {
        callNative(METHOD_SHARE_PROVIDER_CONFIG, JSONObject().apply { put("id", id) }, callback)
    }

    /** 拉取余额（OpenAI+已启用余额配置）。返回：{balance|error} */
    fun getProviderBalance(id: String, callback: CallbackFn?) {
        callNative(METHOD_GET_PROVIDER_BALANCE, JSONObject().apply { put("id", id) }, callback)
    }

    /** 切换供应商类型（openai/google/claude） */
    fun switchProviderType(id: String, newType: String, callback: CallbackFn?) {
        callNative(
            METHOD_SWITCH_PROVIDER_TYPE,
            JSONObject().apply {
                put("id", id)
                put("newType", newType)
            },
            callback
        )
    }

    /** 推荐供应商列表。返回：{recommended:[{id,name,type,baseUrl}]} */
    fun getRecommendedProviders(callback: CallbackFn?) {
        callNative(METHOD_GET_RECOMMENDED_PROVIDERS, null, callback)
    }

    /** 一键添加推荐供应商 */
    fun addRecommendedProvider(recId: String, callback: CallbackFn?) {
        callNative(METHOD_ADD_RECOMMENDED_PROVIDER, JSONObject().apply { put("recId", recId) }, callback)
    }

    /** 自定义添加供应商。返回：{ok, id} */
    fun addCustomProvider(name: String, type: String, baseUrl: String, apiKey: String, callback: CallbackFn?) {
        callNative(
            METHOD_ADD_CUSTOM_PROVIDER,
            JSONObject().apply {
                put("name", name)
                put("type", type)
                put("baseUrl", baseUrl)
                put("apiKey", apiKey)
            },
            callback
        )
    }

    /** 供应商排序（上移/下移） */
    fun moveProvider(from: Int, to: Int, callback: CallbackFn?) {
        callNative(
            METHOD_MOVE_PROVIDER,
            JSONObject().apply {
                put("from", from)
                put("to", to)
            },
            callback
        )
    }

    // ==================== 搜索服务（真实数据） ====================

    /** 搜索服务列表。返回：{selected:index, services:[{id,name,type,hasApiKey}]} */
    fun getSearchServices(callback: CallbackFn?) {
        callNative(METHOD_GET_SEARCH_SERVICES, null, callback)
    }

    /** 设置当前选中的搜索服务（索引） */
    fun setSearchSelected(index: Int, callback: CallbackFn?) {
        callNative(METHOD_SET_SEARCH_SELECTED, JSONObject().apply { put("index", index) }, callback)
    }

    // ==================== TTS（真实数据） ====================

    /** TTS 供应商列表。返回：{providers:[{id,name,type,isDefault}]} */
    fun getTtsProviders(callback: CallbackFn?) {
        callNative(METHOD_GET_TTS_PROVIDERS, null, callback)
    }

    /** 设置默认 TTS 引擎 */
    fun setDefaultTts(id: String, callback: CallbackFn?) {
        callNative(METHOD_SET_DEFAULT_TTS, JSONObject().apply { put("id", id) }, callback)
    }

    /** TTS 引擎详情。返回：{id,name,type,isDefault,apiKey,baseUrl,model,voice} */
    fun getTtsDetail(id: String, callback: CallbackFn?) {
        callNative(METHOD_GET_TTS_DETAIL, JSONObject().apply { put("id", id) }, callback)
    }

    /** 编辑 TTS 基础字段（name/apiKey/baseUrl 传空串=不改） */
    fun updateTtsBasic(id: String, name: String, apiKey: String, baseUrl: String, callback: CallbackFn?) {
        callNative(
            METHOD_UPDATE_TTS_BASIC,
            JSONObject().apply {
                put("id", id)
                put("name", name)
                put("apiKey", apiKey)
                put("baseUrl", baseUrl)
            },
            callback
        )
    }

    /** 编辑 TTS（原始参数版：按需组 JSON） */
    fun updateTtsRaw(params: JSONObject, callback: CallbackFn?) {
        callNative(METHOD_UPDATE_TTS_BASIC, params, callback)
    }

    /** 删除 TTS 引擎 */
    fun deleteTtsProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_TTS_PROVIDER, JSONObject().apply { put("id", id) }, callback)
    }

    /** 添加 TTS 引擎。返回：{ok, id} */
    fun addTtsProvider(type: String, callback: CallbackFn?) {
        callNative(METHOD_ADD_TTS_PROVIDER, JSONObject().apply { put("type", type) }, callback)
    }

    /** 设置默认 TTS 播放速度（0.5-2.0） */
    fun setTtsPlaybackSpeed(speed: Double, callback: CallbackFn?) {
        callNative(METHOD_SET_TTS_PLAYBACK_SPEED, JSONObject().apply { put("speed", speed) }, callback)
    }

    /** ASR 供应商列表。返回：{selected, providers:[{id,name,type,isDefault}]} */
    fun getASRProviders(callback: CallbackFn?) {
        callNative(METHOD_GET_ASR_PROVIDERS, null, callback)
    }

    /** 设置默认 ASR */
    fun setDefaultASR(id: String, callback: CallbackFn?) {
        callNative(METHOD_SET_DEFAULT_ASR, JSONObject().apply { put("id", id) }, callback)
    }

    /** 删除 ASR 供应商 */
    fun deleteAsrProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_ASR_PROVIDER, JSONObject().apply { put("id", id) }, callback)
    }

    // ==================== MCP（真实数据） ====================

    /** MCP 服务器列表。返回：{servers:[{id,name,url,type,enable}]} */
    fun getMcpServers(callback: CallbackFn?) {
        callNative(METHOD_GET_MCP_SERVERS, null, callback)
    }

    /** 启用/禁用 MCP 服务器 */
    fun setMcpEnabled(id: String, enable: Boolean, callback: CallbackFn?) {
        callNative(
            METHOD_SET_MCP_ENABLED,
            JSONObject().apply {
                put("id", id)
                put("enable", enable)
            },
            callback
        )
    }

    /** 删除 MCP 服务器 */
    fun deleteMcpServer(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_MCP_SERVER, JSONObject().apply { put("id", id) }, callback)
    }

    // ==================== Web 服务器（真实数据） ====================

    /** Web 服务器状态。返回：{enabled,running,port,localhostOnly,jwtEnabled,hasPassword,address} */
    fun getWebServerState(callback: CallbackFn?) {
        callNative(METHOD_GET_WEB_SERVER_STATE, null, callback)
    }

    /** 启停 Web 服务器（联动原生 Service） */
    fun setWebServerEnabled(enabled: Boolean, callback: CallbackFn?) {
        callNative(METHOD_SET_WEB_SERVER_ENABLED, JSONObject().apply { put("enabled", enabled) }, callback)
    }

    /** 更新 Web 服务器配置（port/password/localhostOnly/jwtEnabled，仅传需要改的） */
    fun setWebServerConfig(
        port: Int? = null,
        password: String? = null,
        localhostOnly: Boolean? = null,
        jwtEnabled: Boolean? = null,
        callback: CallbackFn? = null
    ) {
        callNative(
            METHOD_SET_WEB_SERVER_CONFIG,
            JSONObject().apply {
                port?.let { put("port", it) }
                password?.let { put("password", it) }
                localhostOnly?.let { put("localhostOnly", it) }
                jwtEnabled?.let { put("jwtEnabled", it) }
            },
            callback
        )
    }

    // ==================== 提示词（模式注入/世界书，真实数据） ====================

    /** 模式注入列表。返回：{modes:[{id,name,enabled,position,contentLength}]} */
    fun getModeInjections(callback: CallbackFn?) {
        callNative(METHOD_GET_MODE_INJECTIONS, null, callback)
    }

    /** 保存模式注入（id 空=新建） */
    fun saveModeInjection(id: String, name: String, content: String, callback: CallbackFn?) {
        callNative(
            METHOD_SAVE_MODE_INJECTION,
            JSONObject().apply {
                put("id", id)
                put("name", name)
                put("content", content)
            },
            callback
        )
    }

    /** 删除模式注入 */
    fun deleteModeInjection(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_MODE_INJECTION, JSONObject().apply { put("id", id) }, callback)
    }

    /** 启停模式注入 */
    fun setModeInjectionEnabled(id: String, enabled: Boolean, callback: CallbackFn?) {
        callNative(
            METHOD_SET_MODE_INJECTION_ENABLED,
            JSONObject().apply {
                put("id", id)
                put("enabled", enabled)
            },
            callback
        )
    }

    /** 世界书列表。返回：{lorebooks:[{id,name,description,enabled,entryCount}]} */
    fun getLorebooks(callback: CallbackFn?) {
        callNative(METHOD_GET_LOREBOOKS, null, callback)
    }

    /** 删除世界书 */
    fun deleteLorebook(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_LOREBOOK, JSONObject().apply { put("id", id) }, callback)
    }

    /** 启停世界书 */
    fun setLorebookEnabled(id: String, enabled: Boolean, callback: CallbackFn?) {
        callNative(
            METHOD_SET_LOREBOOK_ENABLED,
            JSONObject().apply {
                put("id", id)
                put("enabled", enabled)
            },
            callback
        )
    }

    // ==================== 助手（真实数据） ====================

    /** 助手详情。返回：{id,name,systemPrompt,temperature,topP,contextMessageLimit,streamOutput,enableMemory,useGlobalMemory,enableRecentChatsReference,maxTokens,enableWebSearch} */
    fun getAssistantDetail(id: String, callback: CallbackFn?) {
        callNative(METHOD_GET_ASSISTANT_DETAIL, JSONObject().apply { put("id", id) }, callback)
    }

    /** 编辑助手（原始参数版） */
    fun updateAssistantRaw(params: JSONObject, callback: CallbackFn?) {
        callNative(METHOD_UPDATE_ASSISTANT, params, callback)
    }

    // ==================== 模型设置（真实数据：SettingsStore） ====================

    /**
     * 获取模型设置摘要：7 槽位当前模型 + 全部可选 chat 模型 + 建议开关。
     * 返回：{ slots: {chat:{id,name}|{},...}, enableSuggestion, models:[{id,name,provider}] }
     */
    fun getSettingsSummary(callback: CallbackFn?) {
        callNative(METHOD_GET_SETTINGS_SUMMARY, null, callback)
    }

    /** 设置某槽位模型。modelId 传 null 仅对 title/suggestion 有效（清除） */
    fun setModelSlot(slot: String, modelId: String?, callback: CallbackFn?) {
        callNative(
            METHOD_SET_MODEL_SLOT,
            JSONObject().apply {
                put("slot", slot)
                put("modelId", modelId ?: "null")
            },
            callback
        )
    }

    /** 启用/禁用回复建议 */
    fun setEnableSuggestion(enabled: Boolean, callback: CallbackFn?) {
        callNative(METHOD_SET_ENABLE_SUGGESTION, JSONObject().apply { put("enabled", enabled) }, callback)
    }

    // ==================== 日志（请求日志页，真实数据） ====================

    /**
     * 获取最近日志列表（含请求日志开关状态）。
     * 返回：{ enabled: Boolean, logs: [{id, type("text"|"request"), timeText, tag, message?, url?, method?, responseCode?, durationMs?, error?}] }
     */
    fun getLogs(callback: CallbackFn?) {
        callNative(METHOD_GET_LOGS, null, callback)
    }

    /** 获取单条请求日志完整详情（含 headers/body）。返回：{id, url, method, timeFull, responseCode?, durationMs?, error?, requestHeaders, responseHeaders, requestBody?} */
    fun getLogDetail(id: String, callback: CallbackFn?) {
        callNative(METHOD_GET_LOG_DETAIL, JSONObject().apply { put("id", id) }, callback)
    }

    /** 清空日志 */
    fun clearLogs(callback: CallbackFn?) {
        callNative(METHOD_CLEAR_LOGS, null, callback)
    }

    /** 开关请求日志记录 */
    fun setRequestLoggingEnabled(enabled: Boolean, callback: CallbackFn?) {
        callNative(METHOD_SET_REQUEST_LOGGING, JSONObject().apply { put("enabled", enabled) }, callback)
    }

    /** 复制文本到剪贴板 */
    fun copyToClipboard(text: String, callback: CallbackFn?) {
        callNative(METHOD_COPY_TO_CLIPBOARD, JSONObject().apply { put("text", text) }, callback)
    }

    // ==================== 设置/应用信息 ====================

    /** 获取应用版本信息 */
    fun getAppInfo(callback: CallbackFn?) {
        callNative(METHOD_GET_APP_INFO, null, callback)
    }

    /** 检查更新 */
    fun checkUpdate(callback: CallbackFn?) {
        callNative(METHOD_CHECK_UPDATE, null, callback)
    }

    /** 获取设置项（key-value） */
    fun getSetting(key: String, callback: CallbackFn?) {
        callNative(METHOD_GET_SETTING, JSONObject().apply { put("key", key) }, callback)
    }

    /** 保存设置项（key-value） */
    fun setSetting(key: String, value: String, callback: CallbackFn?) {
        callNative(
            METHOD_SET_SETTING,
            JSONObject().apply {
                put("key", key)
                put("value", value)
            },
            callback
        )
    }

    // ==================== 内部实现 ====================

    private fun callNative(methodName: String, data: JSONObject?, callbackFn: CallbackFn?) {
        toNative(false, methodName, data?.toString(), callbackFn, false)
    }

    companion object {
        const val MODULE_NAME = "HelixBridgeModule"

        // 方法名常量（宿主侧据此分发）
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
        const val METHOD_GET_MODE_INJECTIONS = "getModeInjections"
        const val METHOD_SAVE_MODE_INJECTION = "saveModeInjection"
        const val METHOD_DELETE_MODE_INJECTION = "deleteModeInjection"
        const val METHOD_SET_MODE_INJECTION_ENABLED = "setModeInjectionEnabled"
        const val METHOD_GET_LOREBOOKS = "getLorebooks"
        const val METHOD_DELETE_LOREBOOK = "deleteLorebook"
        const val METHOD_SET_LOREBOOK_ENABLED = "setLorebookEnabled"
        const val METHOD_GET_ASSISTANT_DETAIL = "getAssistantDetail"
        const val METHOD_UPDATE_ASSISTANT = "updateAssistant"
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
    }
}
