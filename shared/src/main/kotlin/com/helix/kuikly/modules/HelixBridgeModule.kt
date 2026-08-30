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

    /** 删除供应商 */
    fun deleteProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_DELETE_PROVIDER, JSONObject().apply { put("id", id) }, callback)
    }

    /** 测试供应商连通性（原生 listModels）。返回：{ok, modelCount|error} */
    fun testProvider(id: String, callback: CallbackFn?) {
        callNative(METHOD_TEST_PROVIDER, JSONObject().apply { put("id", id) }, callback)
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
    }
}
