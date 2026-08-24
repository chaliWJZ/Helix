package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.theme.HelixTheme

/**
 * 界面偏好页面（对应 Compose 的 SettingPreferencesUIPage）
 *
 * UI 相关开关：紧凑模式/消息气泡/字体大小等
 */
@Page("SettingPreferencesUIPage")
internal class SettingPreferencesUIPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var compactMode by observable(false)
    private var bubbleStyle by observable(true)
    private var showAvatar by observable(true)
    private var fullWidthMessage by observable(false)
    private var showTokenCount by observable(false)
    private var showTimestamp by observable(true)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "界面",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                        }
                        SwitchRow(
                            title = "紧凑模式",
                            subtitle = "减少消息间距，显示更多内容",
                            checked = page.compactMode,
                            onCheckedChange = { page.compactMode = it }
                        )
                        SwitchRow(
                            title = "气泡样式",
                            subtitle = "消息使用气泡样式展示",
                            checked = page.bubbleStyle,
                            onCheckedChange = { page.bubbleStyle = it }
                        )
                        SwitchRow(
                            title = "显示头像",
                            subtitle = "在消息旁显示用户/助手头像",
                            checked = page.showAvatar,
                            onCheckedChange = { page.showAvatar = it }
                        )
                        SwitchRow(
                            title = "全宽消息",
                            subtitle = "消息占满整行宽度",
                            checked = page.fullWidthMessage,
                            onCheckedChange = { page.fullWidthMessage = it }
                        )
                        SwitchRow(
                            title = "显示 Token 数",
                            subtitle = "在消息底部显示 Token 消耗",
                            checked = page.showTokenCount,
                            onCheckedChange = { page.showTokenCount = it }
                        )
                        SwitchRow(
                            title = "显示时间戳",
                            subtitle = "在消息旁显示发送时间",
                            checked = page.showTimestamp,
                            onCheckedChange = { page.showTimestamp = it }
                        )
                    }
                }
            }
        }
    }
}
