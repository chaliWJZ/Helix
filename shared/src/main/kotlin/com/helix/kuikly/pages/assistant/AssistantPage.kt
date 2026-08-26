package com.helix.kuikly.pages.assistant

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.components.ui.NavBarAction
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 助手页面（对应 Compose 的 AssistantPage）
 *
 * AI 助手列表：创建/编辑助手角色
 */
@Page("AssistantPage")
internal class AssistantPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class Assistant(
        val id: String,
        val name: String,
        val description: String,
        val emoji: String
    )

    private val assistants by observableList<Assistant>()

    override fun created() {
        super.created()
        listOf(
            Assistant("1", "默认助手", "通用的 AI 聊天助手", "🤖"),
            Assistant("2", "翻译专家", "专业多语言翻译", "🌐"),
            Assistant("3", "代码助手", "编程问题解答和代码生成", "💻"),
            Assistant("4", "写作助手", "文章写作和润色", "✍️"),
            Assistant("5", "学习教练", "制定学习计划并答疑", "📚")
        ).forEach { assistants.add(it) }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "助手",
                    showBack = true,
                    onBack = { ctx.router().closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "＋") { ctx.router().openPage("AssistantDetailPage") }
                    )
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ ctx.assistants }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                borderRadius(16f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            event {
                                click {
                                    safeRun("[AssistantPage#body] 助手列表页-助手项") {
                                        KLog.i("HelixKuikly", "👆 [Click] [AssistantPage#body] 助手列表页-助手项")
                                        ctx.router().openPage("AssistantDetailPage")
                                
                                    }
                                
                                }
                            }
                            // 助手头像
                            View {
                                attr {
                                    width(48f)
                                    height(48f)
                                    borderRadius(24f)
                                    backgroundColor(HelixTheme.current.primaryContainer)
                                    justifyContentCenter()
                                    alignItemsCenter()
                                }
                                Text {
                                    attr {
                                        text(item.emoji)
                                        fontSize(24f)
                                    }
                                }
                            }
                            // 名称和描述
                            View {
                                attr {
                                    flex(1f)
                                    marginLeft(12f)
                                }
                                Text {
                                    attr {
                                        text(item.name)
                                        fontSize(16f)
                                        color(HelixTheme.current.onSurface)
                                    }
                                }
                                Text {
                                    attr {
                                        text(item.description)
                                        fontSize(13f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            // 箭头
                            Text {
                                attr {
                                    text("›")
                                    fontSize(20f)
                                    color(HelixTheme.current.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
