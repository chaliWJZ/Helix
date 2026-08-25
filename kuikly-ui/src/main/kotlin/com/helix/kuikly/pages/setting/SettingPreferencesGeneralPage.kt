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
 * 通用偏好页面（对应 Compose 的 SettingPreferencesGeneralPage）
 *
 * 聊天行为相关开关设置
 */
@Page("SettingPreferencesGeneralPage")
internal class SettingPreferencesGeneralPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var createNewConversationOnStart by observable(true)
    private var sendOnEnter by observable(true)
    private var showMessageJumper by observable(true)
    private var messageJumperOnLeft by observable(false)
    private var enableAutoScroll by observable(true)
    private var useAppIconStyleLoading by observable(false)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "通用",
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
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                        }
                        SwitchRow(
                            title = "启动时新建会话",
                            subtitle = "打开应用时自动创建新会话",
                            checked = page.createNewConversationOnStart,
                            onCheckedChange = { page.createNewConversationOnStart = it }
                        )
                        SwitchRow(
                            title = "回车发送",
                            subtitle = "按回车键直接发送消息",
                            checked = page.sendOnEnter,
                            onCheckedChange = { page.sendOnEnter = it }
                        )
                        SwitchRow(
                            title = "显示消息跳转按钮",
                            subtitle = "在消息列表边缘显示快速跳转按钮",
                            checked = page.showMessageJumper,
                            onCheckedChange = { page.showMessageJumper = it }
                        )
                        SwitchRow(
                            title = "跳转按钮在左侧",
                            subtitle = "将消息跳转按钮显示在左侧",
                            checked = page.messageJumperOnLeft,
                            onCheckedChange = { page.messageJumperOnLeft = it }
                        )
                        SwitchRow(
                            title = "自动滚动",
                            subtitle = "新消息到达时自动滚动到底部",
                            checked = page.enableAutoScroll,
                            onCheckedChange = { page.enableAutoScroll = it }
                        )
                        SwitchRow(
                            title = "应用图标加载动画",
                            subtitle = "使用应用图标风格的加载指示器",
                            checked = page.useAppIconStyleLoading,
                            onCheckedChange = { page.useAppIconStyleLoading = it }
                        )
                    }
                }
            }
        }
    }
}
