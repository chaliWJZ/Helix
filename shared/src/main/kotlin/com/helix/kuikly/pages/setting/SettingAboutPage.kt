package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 关于页面（对应 Compose 的 SettingAboutPage）
 */
@Page("SettingAboutPage")
internal class SettingAboutPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "关于",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(16f)
                        alignItemsCenter()
                    }
                    // 应用 Logo
                    View {
                        attr {
                            width(80f)
                            height(80f)
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.primaryContainer)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("H")
                                fontSize(40f)
                                color(HelixTheme.current.primary)
                            }
                        }
                    }
                    Text {
                        attr {
                            text("Helix")
                            fontSize(24f)
                            color(HelixTheme.current.onBackground)
                            marginTop(12f)
                        }
                    }
                    Text {
                        attr {
                            text("v1.0.0")
                            fontSize(14f)
                            color(HelixTheme.current.onSurfaceVariant)
                            marginTop(4f)
                        }
                    }
                }
                // 信息卡片
                View {
                    attr {
                        marginTop(24f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "GitHub",
                                subtitle = "查看源代码和提交 Issue",
                                icon = "🐙",
                                onClick = {
                                    val params = JSONObject()
                                    params.put("url", "https://github.com/chaliWJZ/Helix")
                                    ctx.router().openPage("WebViewPage", params)
                                }
                            ),
                            CardGroupItem(
                                title = "开源许可",
                                subtitle = "查看使用的开源组件",
                                icon = "📄",
                                onClick = {
                                    // 修复 stbug B1：SettingAboutLicensePage 未实现会白屏，
                                    // 改为对齐 Compose 版行为（openUrl 打开 LICENSE 链接）
                                    val params = JSONObject()
                                    params.put("url", "https://github.com/chaliWJZ/Helix/blob/master/LICENSE")
                                    params.put("title", "开源许可")
                                    ctx.router().openPage("WebViewPage", params)
                                }
                            )
                        )
                    )
                }
            }
        }
    }
}
