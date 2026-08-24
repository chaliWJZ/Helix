package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.theme.HelixTheme

/**
 * Web 服务器页面（对应 Compose 的 SettingWebPage）
 *
 * 局域网 Web 访问开关 + 端口/密钥配置
 */
@Page("SettingWebPage")
internal class SettingWebPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var webServerEnabled by observable(false)
    private var webServerPort by observable("8080")
    private var webApiKey by observable("")

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "Web 服务器",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 服务器开关
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(16f)
                        }
                        SwitchRow(
                            title = "启用 Web 服务器",
                            subtitle = "在浏览器中访问 Helix 聊天界面",
                            checked = page.webServerEnabled,
                            onCheckedChange = { page.webServerEnabled = it }
                        )
                    }
                    // 配置信息
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            padding(16f)
                        }
                        Text {
                            attr {
                                text("端口")
                                fontSize(14f)
                                color(HelixTheme.Light.onSurfaceVariant)
                            }
                        }
                        Text {
                            attr {
                                text(page.webServerPort)
                                fontSize(16f)
                                color(HelixTheme.Light.onSurface)
                                marginTop(4f)
                            }
                        }
                        Text {
                            attr {
                                text("API 密钥")
                                fontSize(14f)
                                color(HelixTheme.Light.onSurfaceVariant)
                                marginTop(16f)
                            }
                        }
                        Text {
                            attr {
                                text(if (page.webApiKey.isEmpty()) "未设置" else page.webApiKey)
                                fontSize(16f)
                                color(HelixTheme.Light.onSurface)
                                marginTop(4f)
                            }
                        }
                    }
                }
            }
        }
    }
}
