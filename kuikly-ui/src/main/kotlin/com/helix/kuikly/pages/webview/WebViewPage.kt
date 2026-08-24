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
        // 触发原生 WebView 打开
        if (url.isNotEmpty()) {
            loading = true
            bridge().openWebView(url, title)
        }
    }

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = page.title,
                    showBack = true,
                    onBack = { page.closePage() }
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
                            text(page.url)
                            fontSize(13f)
                            color(HelixTheme.Light.primary)
                            marginTop(8f)
                        }
                    }
                    Text {
                        attr {
                            text(
                                if (page.loading) "正在通过原生 WebView 加载..." else "WebView 已交给原生渲染"
                            )
                            fontSize(12f)
                            color(HelixTheme.Light.onSurfaceVariant)
                            marginTop(4f)
                        }
                    }
                }
            }
        }
    }
}
