package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.theme.HelixTheme

/**
 * 主题偏好页面（对应 Compose 的 SettingPreferencesThemePage）
 */
@Page("SettingPreferencesThemePage")
internal class SettingPreferencesThemePage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var dynamicColor by observable(false)
    private var amoledDark by observable(false)
    private var pureBlackBg by observable(false)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "主题",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "颜色模式",
                                subtitle = "选择浅色/深色/跟随系统",
                                icon = "🎨",
                                onClick = { /* 颜色选择器 */ }
                            )
                        )
                    )
                    // 开关项
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(8f)
                        }
                        SwitchRow(
                            title = "动态取色",
                            subtitle = "Material You 壁纸取色（Android 12+）",
                            checked = page.dynamicColor,
                            onCheckedChange = { page.dynamicColor = it }
                        )
                        SwitchRow(
                            title = "AMOLED 暗色模式",
                            subtitle = "纯黑背景省电",
                            checked = page.amoledDark,
                            onCheckedChange = { page.amoledDark = it }
                        )
                        SwitchRow(
                            title = "纯黑背景",
                            subtitle = "聊天页面使用纯黑背景",
                            checked = page.pureBlackBg,
                            onCheckedChange = { page.pureBlackBg = it }
                        )
                    }
                }
            }
        }
    }
}
