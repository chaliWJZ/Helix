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
 * 工作区列表页面（对应 Compose 的 WorkspacePage）
 *
 * 工作区管理：文件工作区列表
 */
@Page("WorkspacePage")
internal class WorkspacePage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "工作区",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "＋") { ctx.openPage("WorkspaceDetailPage") }
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
                                title = "默认工作区",
                                subtitle = "12 个文件 · 上次编辑：今天",
                                icon = "📁",
                                onClick = { ctx.openPage("WorkspaceDetailPage") }
                            ),
                            CardGroupItem(
                                title = "项目文档",
                                subtitle = "5 个文件 · 上次编辑：昨天",
                                icon = "📁",
                                onClick = { ctx.openPage("WorkspaceDetailPage") }
                            ),
                            CardGroupItem(
                                title = "代码片段",
                                subtitle = "28 个文件 · 上次编辑：8月20日",
                                icon = "📁",
                                onClick = { ctx.openPage("WorkspaceDetailPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
