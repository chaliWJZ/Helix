package com.helix.app.helix.kuikly

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONObject

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
                    }
                    context?.startActivity(Intent.createChooser(intent, "分享"))
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

            METHOD_READ_FILE, METHOD_WRITE_FILE, METHOD_LIST_FILES,
            METHOD_PICK_FILE, METHOD_TAKE_PHOTO, METHOD_PICK_IMAGE,
            METHOD_CHECK_UPDATE -> {
                callback?.invoke(JSONObject().apply { put("error", "not_implemented_yet") })
                null
            }

            else -> super.call(method, params, callback)
        }
    }

    /** URL 安全校验：仅允许 http/https */
    private fun isSafeUrl(url: String): Boolean {
        if (url.isEmpty() || url.length > MAX_URL_LENGTH) return false
        val scheme = runCatching { Uri.parse(url).scheme?.lowercase() }.getOrNull()
        return scheme == "http" || scheme == "https"
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
        private const val MAX_URL_LENGTH = 2048
        /** 内部约定 scheme：跳 Compose 版对比 */
        const val COMPOSE_DEBUG_URL = "helix://compose-debug"
    }
}
