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
 * 通知偏好页面（对应 Compose 的 SettingPreferencesNotificationPage）
 */
@Page("SettingPreferencesNotificationPage")
internal class SettingPreferencesNotificationPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private var enableNotification by observable(true)
    private var notificationSound by observable(true)
    private var notificationVibrate by observable(false)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "通知",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
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
                            marginBottom(8f)
                        }
                        SwitchRow(
                            title = "启用通知",
                            subtitle = "接收消息和提醒通知",
                            checked = { ctx.enableNotification },
                            onCheckedChange = { ctx.enableNotification = it }
                        )
                        SwitchRow(
                            title = "通知声音",
                            subtitle = "通知时播放提示音",
                            checked = { ctx.notificationSound },
                            onCheckedChange = { ctx.notificationSound = it }
                        )
                        SwitchRow(
                            title = "振动",
                            subtitle = "通知时振动提醒",
                            checked = { ctx.notificationVibrate },
                            onCheckedChange = { ctx.notificationVibrate = it }
                        )
                    }
                }
            }
        }
    }
}
