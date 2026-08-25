package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.RadioRow
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.helix.kuikly.theme.ThemeManager

/**
 * 主题偏好页面（颜色模式单选）
 *
 * 3 个选项：跟随系统 / 浅色 / 深色
 * 点击立即切换全局主题（ThemeManager.isDark observable 驱动所有页面刷新）
 */
@Page("SettingPreferencesThemePage")
internal class SettingPreferencesThemePage : HelixBasePage() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    /** 颜色模式：0=跟随系统 1=浅色 2=深色 */
    internal var colorMode by observable(0)

    override fun created() {
        super.created()
        // 初始化：跟随系统模式下读系统当前值
        if (ThemeManager.followSystem) {
            colorMode = 0
        }
    }

    /** 选择模式并立即应用主题 */
    private fun selectMode(mode: Int) {
        colorMode = mode
        when (mode) {
            0 -> {
                // 跟随系统：重新读系统主题
                ThemeManager.followSystem = true
                val sysTheme = runCatching { pageData.params.optString("sysTheme") }.getOrNull()
                ThemeManager.initFromSystem(sysTheme)
            }
            1 -> {
                // 浅色
                ThemeManager.followSystem = false
                ThemeManager.isDark = false
            }
            2 -> {
                // 深色
                ThemeManager.followSystem = false
                ThemeManager.isDark = true
            }
        }
    }

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
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    RadioRow(
                        title = "跟随系统",
                        subtitle = "跟随系统切换",
                        icon = "📱",
                        selected = page.colorMode == 0,
                        onClick = { page.selectMode(0) }
                    )
                    RadioRow(
                        title = "浅色",
                        subtitle = "始终使用浅色模式",
                        icon = "☀️",
                        selected = page.colorMode == 1,
                        onClick = { page.selectMode(1) }
                    )
                    RadioRow(
                        title = "深色",
                        subtitle = "始终使用深色模式",
                        icon = "🌙",
                        selected = page.colorMode == 2,
                        onClick = { page.selectMode(2) }
                    )
                    // 说明文字
                    Text {
                        attr {
                            text("点击选项立即生效；跟随系统模式在系统切换日夜间时自动跟随")
                            fontSize(12f)
                            color(HelixTheme.current.onSurfaceVariant)
                            marginLeft(20f)
                            marginRight(20f)
                            marginTop(16f)
                        }
                    }
                }
            }
        }
    }
}
