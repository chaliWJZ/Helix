package com.helix.kuikly.pages.extensions

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 扩展页面（对应 Compose 的 ExtensionsPage）
 *
 * 4 个扩展入口：快捷消息/提示词/技能/工作区
 */
@Page("ExtensionsPage")
internal class ExtensionsPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "扩展",
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
                                title = "快捷消息",
                                subtitle = "快速发送预设消息",
                                icon = "⚡",
                                onClick = { page.router().openPage("QuickMessagesPage") }
                            ),
                            CardGroupItem(
                                title = "提示词",
                                subtitle = "管理常用提示词模板",
                                icon = "📖",
                                onClick = { page.router().openPage("PromptPage") }
                            ),
                            CardGroupItem(
                                title = "技能",
                                subtitle = "Agent 技能扩展",
                                icon = "🧩",
                                onClick = { page.router().openPage("SkillsPage") }
                            ),
                            CardGroupItem(
                                title = "工作区",
                                subtitle = "文件工作区和终端",
                                icon = "📁",
                                onClick = { page.router().openPage("WorkspacePage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
