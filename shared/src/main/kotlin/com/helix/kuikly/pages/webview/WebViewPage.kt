package com.helix.kuikly.pages.webview

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * WebView 页面（对应 Compose 的 WebViewPage）
 *
 * 通过 HelixBridgeModule 桥接原生 WebView。
 * Kuikly 侧负责：接收 url 参数、触发原生 WebView、展示加载状态。
 */
@Page("WebViewPage")
internal class WebViewPage : HelixBasePage() {

    private var url: String = ""
    private var title: String = ""
    private var loading by observable(false)

    override fun created() {
        super.created()
        // 从页面参数中取出 url（由 openPage 传入）
        url = pageData.params.optString("url")
        title = pageData.params.optString("title")
        if (title.isEmpty()) {
            title = "网页"
        }
        // 触发原生 WebView 打开（scheme 白名单校验：仅允许 http/https）
        if (isSafeUrl(url)) {
            loading = true
            bridge().openWebView(url, title)
        } else if (url.isNotEmpty()) {
            // 非白名单 scheme（javascript:/file:/intent: 等），拒绝打开
            loading = false
        }
    }

    /**
     * URL 安全校验：仅允许 http/https scheme，且长度不超过 2048
     * （第一道防线；原生侧 HelixRenderModule.isSafeUrl 为第二道防线）
     */
    private fun isSafeUrl(url: String): Boolean {
        if (url.isEmpty() || url.length > 2048) return false
        val lower = url.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = ctx.title,
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                // 内容区（原生 WebView 覆盖在此区域上，此处为占位 UI）
                View {
                    attr {
                        flex(1f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text("🌐")
                            fontSize(48f)
                        }
                    }
                    Text {
                        attr {
                            text(ctx.url)
                            fontSize(13f)
                            color(HelixTheme.current.primary)
                            marginTop(8f)
                        }
                    }
                    Text {
                        attr {
                            text(
                                if (ctx.loading) "正在通过原生 WebView 加载..." else "WebView 已交给原生渲染"
                            )
                            fontSize(12f)
                            color(HelixTheme.current.onSurfaceVariant)
                            marginTop(4f)
                        }
                    }
                }
            }
        }
    }
}
