package com.helix.kuikly.pages.workspace

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 工作区详情页面（对应 Compose 的 WorkspaceDetailPage）
 *
 * 文件浏览：文件列表 + 终端入口 + 编辑器入口
 */
@Page("WorkspaceDetailPage")
internal class WorkspaceDetailPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "默认工作区",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "🖥️") { ctx.openPage("WorkspaceTerminalPage") }
                    )
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "README.md",
                                subtitle = "Markdown · 2.1 KB",
                                icon = "📝",
                                onClick = { ctx.openPage("WorkspaceFileEditorPage") }
                            ),
                            CardGroupItem(
                                title = "main.kt",
                                subtitle = "Kotlin · 5.4 KB",
                                icon = "📄",
                                onClick = { ctx.openPage("WorkspaceFileEditorPage") }
                            ),
                            CardGroupItem(
                                title = "config.json",
                                subtitle = "JSON · 480 B",
                                icon = "⚙️",
                                onClick = { ctx.openPage("WorkspaceFileEditorPage") }
                            ),
                            CardGroupItem(
                                title = "notes.txt",
                                subtitle = "文本 · 1.2 KB",
                                icon = "🗒️",
                                onClick = { ctx.openPage("WorkspaceFileEditorPage") }
                            ),
                            CardGroupItem(
                                title = "终端",
                                subtitle = "打开 Termux 终端环境",
                                icon = "💻",
                                onClick = { ctx.openPage("WorkspaceTerminalPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
