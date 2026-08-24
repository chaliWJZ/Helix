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
 * 供应商列表页面（对应 Compose 的 SettingProviderPage）
 *
 * AI 服务供应商列表：OpenAI/Anthropic/Google/DeepSeek 等
 */
@Page("SettingProviderPage")
internal class SettingProviderPage : Pager() {

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
                    title = "供应商",
                    showBack = true,
                    onBack = { page.router().closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "添加") { page.router().openPage("SettingProviderDetailPage") }
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
                                title = "OpenAI",
                                subtitle = "GPT-4o / o1 / DALL-E 系列",
                                icon = "🤖",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            ),
                            CardGroupItem(
                                title = "Anthropic",
                                subtitle = "Claude 系列模型",
                                icon = "🎭",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            ),
                            CardGroupItem(
                                title = "Google",
                                subtitle = "Gemini 系列模型",
                                icon = "✨",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            ),
                            CardGroupItem(
                                title = "DeepSeek",
                                subtitle = "DeepSeek-V3 / R1 系列",
                                icon = "🐋",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            ),
                            CardGroupItem(
                                title = "OpenRouter",
                                subtitle = "聚合多供应商 API",
                                icon = "🔀",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            ),
                            CardGroupItem(
                                title = "硅基流动",
                                subtitle = "SiliconFlow 国内聚合服务",
                                icon = "⚡",
                                onClick = { page.router().openPage("SettingProviderDetailPage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
