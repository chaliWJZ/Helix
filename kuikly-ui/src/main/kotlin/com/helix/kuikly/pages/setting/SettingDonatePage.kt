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
 * 捐赠页面（对应 Compose 的 SettingDonatePage）
 */
@Page("SettingDonatePage")
internal class SettingDonatePage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "捐赠",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 捐赠方式
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "爱发电",
                                subtitle = "通过爱发电支持本项目",
                                icon = "💡",
                                onClick = {
                                    val params = JSONObject()
                                    params.put("url", "https://afdian.net")
                                    page.router().openPage("WebViewPage", params)
                                }
                            ),
                            CardGroupItem(
                                title = "微信赞赏",
                                subtitle = "通过微信二维码赞赏",
                                icon = "💚",
                                onClick = { /* TODO: 二维码预览 */ }
                            ),
                            CardGroupItem(
                                title = "支付宝",
                                subtitle = "通过支付宝二维码捐赠",
                                icon = "💙",
                                onClick = { /* TODO: 二维码预览 */ }
                            )
                        )
                    )
                    // 赞助者列表
                    Text {
                        attr {
                            text("赞助者列表")
                            fontSize(14f)
                            color(HelixTheme.current.primary)
                            marginLeft(16f)
                            marginTop(24f)
                            marginBottom(8f)
                        }
                    }
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            padding(16f)
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("感谢所有赞助者的支持 ❤️")
                                fontSize(14f)
                                color(HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
