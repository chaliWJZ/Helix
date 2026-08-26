package com.helix.kuikly.pages.history

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 历史记录页面（对应 Compose 的 HistoryPage）
 *
 * 会话历史列表：搜索 + 按时间分组的会话列表
 */
@Page("HistoryPage")
internal class HistoryPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 会话数据模型
    data class Conversation(
        val id: String,
        val title: String,
        val time: String,
        val messageCount: Int
    )

    // 会话列表（占位数据，后续接入 DB Module）
    private val conversations by observableList<Conversation>()
    private var searchKeyword by observable("")

    override fun created() {
        super.created()
        // 占位数据
        listOf(
            Conversation("1", "关于 Kotlin 协程的问题", "今天 14:30", 12),
            Conversation("2", "翻译一段英文文档", "今天 10:15", 4),
            Conversation("3", "帮我写一个 Python 脚本", "昨天 20:45", 28),
            Conversation("4", "AI 发展历史讨论", "昨天 09:20", 45),
            Conversation("5", "周末旅游计划", "8月22日", 8),
            Conversation("6", "代码 Review 建议", "8月21日", 16),
            Conversation("7", "机器学习入门", "8月20日", 67)
        ).forEach { conversations.add(it) }
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
                    title = "历史记录",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 会话列表
                    vfor({ ctx.conversations }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(1f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            event {
                                click {
                                    KLog.i("HelixKuikly", "👆 [Click] 历史记录页-历史会话项")
                                    ctx.router().openPage("ChatPage")
                                }
                            }
                            // 会话图标
                            Text {
                                attr {
                                    text("💬")
                                    fontSize(20f)
                                    marginRight(12f)
                                }
                            }
                            // 标题和摘要
                            View {
                                attr {
                                    flex(1f)
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
                                        text("${item.messageCount} 条消息")
                                        fontSize(13f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            // 时间
                            Text {
                                attr {
                                    text(item.time)
                                    fontSize(12f)
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
