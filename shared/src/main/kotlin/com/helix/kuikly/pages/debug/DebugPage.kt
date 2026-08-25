package com.helix.kuikly.pages.debug

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 调试页面（对应 Compose 的 DebugPage）
 *
 * 开发者调试工具入口
 */
@Page("DebugPage")
internal class DebugPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "调试",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "Kuikly 演示页",
                                subtitle = "查看 Kuikly Markdown 渲染能力",
                                icon = "🎨",
                                onClick = { ctx.openPage("MarkdownDemoPage") }
                            ),
                            CardGroupItem(
                                title = "日志查看",
                                subtitle = "查看应用运行日志",
                                icon = "📋",
                                onClick = { ctx.openPage("LogPage") }
                            ),
                            CardGroupItem(
                                title = "Toast 测试",
                                subtitle = "测试原生桥接 Module",
                                icon = "🔔",
                                onClick = { ctx.bridge().toast("Hello from Kuikly!") }
                            ),
                            CardGroupItem(
                                title = "应用信息",
                                subtitle = "查看版本等基本信息",
                                icon = "ℹ️",
                                onClick = { ctx.bridge().toast("Helix Kuikly v1.0") }
                            ),
                            CardGroupItem(
                                title = "统计信息",
                                subtitle = "使用统计",
                                icon = "📊",
                                onClick = { ctx.openPage("StatsPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
