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
 * 网络偏好页面（对应 Compose 的 SettingPreferencesNetworkPage）
 *
 * 代理设置 / 超时配置
 */
@Page("SettingPreferencesNetworkPage")
internal class SettingPreferencesNetworkPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var useProxy by observable(false)
    private var proxyHost by observable("")
    private var proxyPort by observable("")
    private var connectionTimeout by observable("30")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "网络",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 代理设置
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(16f)
                        }
                        SwitchRow(
                            title = "使用代理",
                            subtitle = "通过 HTTP/SOCKS 代理发送请求",
                            checked = { ctx.useProxy },
                            onCheckedChange = { ctx.useProxy = it }
                        )
                    }
                    // 超时设置
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            padding(16f)
                        }
                        Text {
                            attr {
                                text("连接超时（秒）")
                                fontSize(16f)
                                color(HelixTheme.current.onSurface)
                            }
                        }
                        Text {
                            attr {
                                text(ctx.connectionTimeout)
                                fontSize(28f)
                                color(HelixTheme.current.primary)
                                marginTop(8f)
                            }
                        }
                        Text {
                            attr {
                                text("与 AI 服务建立连接的最长等待时间")
                                fontSize(14f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                            }
                        }
                    }
                }
            }
        }
    }
}
