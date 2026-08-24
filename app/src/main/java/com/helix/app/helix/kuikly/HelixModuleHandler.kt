package com.helix.app.helix.kuikly

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.helix.kuikly.modules.HelixBridgeModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Helix Kuikly Module 宿主侧处理器
 *
 * 处理 Kuikly 页面通过 HelixBridgeModule 发起的原生能力调用。
 * 在 KuiklyEngine 初始化时注册到 delegate 回调中。
 *
 * 用法（在 Kuikly 宿主初始化处）：
 * ```
 * val handler = HelixModuleHandler(context)
 * // 在 IKuiklyCoreEntry Delegate 的 onModuleCall 回调中：
 * handler.handleCall(method, params) { result -> ... }
 * ```
 */
class HelixModuleHandler(private val context: Context) {

    /**
     * 处理来自 Kuikly 页面的 Module 方法调用
     *
     * @param method 方法名（对应 HelixBridgeModule.METHOD_* 常量）
     * @param params 参数 JSON
     * @param callback 结果回调（可为 null）
     * @return 是否已处理该 метод
     */
    fun handleCall(
        method: String,
        params: JSONObject?,
        callback: ((Any?) -> Unit)?
    ): Boolean {
        return when (method) {
            HelixBridgeModule.METHOD_TOAST -> {
                val message = params?.optString("message") ?: ""
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                true
            }

            HelixBridgeModule.METHOD_OPEN_URL -> {
                val url = params?.optString("url") ?: ""
                openExternalUrl(url)
                true
            }

            HelixBridgeModule.METHOD_SHARE -> {
                val text = params?.optString("text") ?: ""
                shareText(text)
                true
            }

            HelixBridgeModule.METHOD_OPEN_WEBVIEW -> {
                val url = params?.optString("url") ?: ""
                val title = params?.optString("title") ?: "网页"
                openWebView(url, title)
                true
            }

            HelixBridgeModule.METHOD_GET_APP_INFO -> {
                callback?.invoke(getAppInfo())
                true
            }

            HelixBridgeModule.METHOD_GET_SETTING -> {
                // TODO: 对接 DataStore Settings
                callback?.invoke(JSONObject().apply { put("value", "") })
                true
            }

            HelixBridgeModule.METHOD_SET_SETTING -> {
                // TODO: 对接 DataStore Settings
                callback?.invoke(true)
                true
            }

            // 文件/相机等需要 Activity 上下文的能力，待接入 KuiklyEngine 后实现
            HelixBridgeModule.METHOD_READ_FILE,
            HelixBridgeModule.METHOD_WRITE_FILE,
            HelixBridgeModule.METHOD_LIST_FILES,
            HelixBridgeModule.METHOD_PICK_FILE,
            HelixBridgeModule.METHOD_TAKE_PHOTO,
            HelixBridgeModule.METHOD_PICK_IMAGE,
            HelixBridgeModule.METHOD_CHECK_UPDATE -> {
                // 占位：返回未实现
                callback?.invoke(JSONObject().apply { put("error", "not_implemented_yet") })
                true
            }

            else -> false
        }
    }

    // ==================== 具体实现 ====================

    private fun openExternalUrl(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun shareText(text: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "分享").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun openWebView(url: String, title: String) {
        // 使用 Compose 版已有的 WebView 路由（过渡期方案）
        // 后续可在 KuiklyEngine 的 NativeView 容器中嵌入原生 WebView
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun getAppInfo(): JSONObject {
        return JSONObject().apply {
            put("versionName", "1.0.0")
            put("versionCode", 1)
            put("packageName", context.packageName)
        }
    }
}
