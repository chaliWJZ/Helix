package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 偏好设置页面（对应 Compose 的 SettingPreferencesPage）
 */
@Page("SettingPreferencesPage")
internal class SettingPreferencesPage : Pager() {

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
                    title = "偏好设置",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "主题",
                                subtitle = "应用主题模式设置",
                                icon = "☀️",
                                onClick = { ctx.router().openPage("SettingPreferencesThemePage") }
                            ),
                            CardGroupItem(
                                title = "通知",
                                subtitle = "通知设置和渠道配置",
                                icon = "🔔",
                                onClick = { ctx.router().openPage("SettingPreferencesNotificationPage") }
                            ),
                            CardGroupItem(
                                title = "通用",
                                subtitle = "通用功能偏好设置",
                                icon = "⚙️",
                                onClick = { ctx.router().openPage("SettingPreferencesGeneralPage") }
                            ),
                            CardGroupItem(
                                title = "界面",
                                subtitle = "界面样式和布局设置",
                                icon = "🎨",
                                onClick = { ctx.router().openPage("SettingPreferencesUIPage") }
                            ),
                            CardGroupItem(
                                title = "网络",
                                subtitle = "网络代理和请求设置",
                                icon = "🌐",
                                onClick = { ctx.router().openPage("SettingPreferencesNetworkPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
