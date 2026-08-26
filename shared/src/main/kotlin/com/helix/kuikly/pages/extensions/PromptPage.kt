package com.helix.kuikly.pages.extensions

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 提示词页面（对应 Compose 的 PromptPage）
 *
 * 提示词模板列表管理
 */
@Page("PromptPage")
internal class PromptPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class Prompt(
        val id: String,
        val title: String,
        val content: String,
        val tags: List<String>
    )

    private val prompts by observableList<Prompt>()

    override fun created() {
        super.created()
        listOf(
            Prompt("1", "代码优化", "请帮我优化以下代码，提高可读性和性能：\n```\n{code}\n```", listOf("编程", "优化")),
            Prompt("2", "文章翻译", "请将以下文章翻译成{lang}，保持原文风格：\n{text}", listOf("翻译")),
            Prompt("3", "周报生成", "根据本周完成的工作内容，生成一份结构清晰的周报", listOf("办公")),
            Prompt("4", "SQL 查询", "根据以下表结构，编写 SQL 查询：{schema}\n需求：{requirement}", listOf("数据库"))
        ).forEach { prompts.add(it) }
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
                    title = "提示词",
                    showBack = true,
                    onBack = { ctx.router().closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "＋") { /* TODO: 新建提示词 */ }
                    )
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ ctx.prompts }) { item ->
                        View {
                            attr {
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                borderRadius(16f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            event {
                                click {
                                    KLog.i("HelixKuikly", "👆 [Click] 提示词页-编辑提示词")
                                    /* TODO: 编辑提示词 */
                                }
                            }
                            Text {
                                attr {
                                    text(item.title)
                                    fontSize(16f)
                                    color(HelixTheme.current.onSurface)
                                }
                            }
                            Text {
                                attr {
                                    text(item.content.take(60) + if (item.content.length > 60) "..." else "")
                                    fontSize(13f)
                                    color(HelixTheme.current.onSurfaceVariant)
                                    marginTop(4f)
                                }
                            }
                            // 标签
                            View {
                                attr {
                                    flexDirectionRow()
                                    marginTop(8f)
                                }
                                item.tags.forEach { tag ->
                                    Text {
                                        attr {
                                            text(tag)
                                            fontSize(11f)
                                            color(HelixTheme.current.primary)
                                            marginLeft(8f)
                                            marginRight(6f)
                                            marginTop(8f)
                                            borderRadius(8f)
                                            backgroundColor(HelixTheme.current.primaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
