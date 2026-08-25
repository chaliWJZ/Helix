package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.RadioRow
import com.helix.kuikly.theme.HelixTheme

/**
 * 主题偏好页面（对应 Compose 的 SettingPreferencesThemePage）
 *
 * 顶部 3 选项单选（颜色模式）+ 底部 3 个开关
 */
@Page("SettingPreferencesThemePage")
internal class SettingPreferencesThemePage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    /** 颜色模式：0=系统 1=浅色 2=深色（与 SettingPage 共享语义） */
    internal var colorMode by observable(0)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "主题",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    // ==================== 颜色模式（3 选项单选 Radio） ====================
                    View {
                        attr {
                            flexDirectionColumn()
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(8f)
                        }
                        RadioRow(
                            title = "跟随系统",
                            subtitle = "跟随系统切换",
                            icon = "📱",
                            selected = page.colorMode == 0,
                            onClick = { page.colorMode = 0 }
                        )
                        RadioRow(
                            title = "浅色",
                            subtitle = "始终使用浅色模式",
                            icon = "☀️",
                            selected = page.colorMode == 1,
                            onClick = { page.colorMode = 1 }
                        )
                        RadioRow(
                            title = "深色",
                            subtitle = "始终使用深色模式",
                            icon = "🌙",
                            selected = page.colorMode == 2,
                            onClick = { page.colorMode = 2 }
                        )
                    }
                }
            }
        }
    }
}
