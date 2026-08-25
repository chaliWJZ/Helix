package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 搜索服务页面（对应 Compose 的 SettingSearchPage）
 *
 * 搜索引擎列表：Bing/Google/SearXNG/Tavily/博查 等
 */
@Page("SettingSearchPage")
internal class SettingSearchPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "搜索服务",
                    showBack = true,
                    onBack = { page.router().closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "添加") { page.router().openPage("SettingSearchDetailPage") }
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
                                title = "Bing",
                                subtitle = "微软必应搜索",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchDetailPage") }
                            ),
                            CardGroupItem(
                                title = "Google",
                                subtitle = "谷歌搜索",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchDetailPage") }
                            ),
                            CardGroupItem(
                                title = "SearXNG",
                                subtitle = "开源聚合搜索引擎",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchDetailPage") }
                            ),
                            CardGroupItem(
                                title = "Tavily",
                                subtitle = "AI 优化的搜索 API",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchDetailPage") }
                            ),
                            CardGroupItem(
                                title = "博查",
                                subtitle = "国内 AI 搜索服务",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchDetailPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
