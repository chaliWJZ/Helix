package com.helix.kuikly.pages.chat

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 聊天抽屉页面（对应 Compose 的 ChatDrawer）
 *
 * 会话列表抽屉：文件夹栏 + 会话列表 + 助手选择 + 底部动作
 */
@Page("ChatDrawerPage")
internal class ChatDrawerPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    /** 供文件级组件使用的路由访问 */
    internal fun routerPublic(): RouterModule = router()

    data class ConversationItem(
        val id: String,
        val title: String,
        val time: String,
        val active: Boolean
    )

    // 文件夹
    private val folders by observableList<String>()
    private var selectedFolder by observable(0)

    // 会话列表
    private val conversations by observableList<ConversationItem>()

    // 当前助手
    private var currentAssistant by observable("默认助手 🤖")

    override fun created() {
        super.created()
        listOf("全部", "默认", "工作", "学习", "娱乐").forEach { folders.add(it) }
        listOf(
            ConversationItem("1", "关于 Kotlin 协程的问题", "14:30", true),
            ConversationItem("2", "翻译一段英文文档", "10:15", false),
            ConversationItem("3", "帮我写一个 Python 脚本", "昨天", false),
            ConversationItem("4", "AI 发展历史讨论", "昨天", false),
            ConversationItem("5", "周末旅游计划", "8月22日", false),
            ConversationItem("6", "代码 Review 建议", "8月21日", false)
        ).forEach { conversations.add(it) }
    }

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.surface)
                }
                // ==================== 顶部：当前助手 ====================
                View {
                    attr {
                        flexDirectionRow()
                        alignItemsCenter()
                        padding(16f)
                        backgroundColor(HelixTheme.Light.surfaceContainerLow)
                    }
                    View {
                        attr {
                            width(40f)
                            height(40f)
                            borderRadius(20f)
                            backgroundColor(HelixTheme.Light.primaryContainer)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("🤖")
                                fontSize(20f)
                            }
                        }
                    }
                    Text {
                        attr {
                            text(page.currentAssistant)
                            fontSize(15f)
                            color(HelixTheme.Light.onSurface)
                            marginLeft(12f)
                            flex(1f)
                        }
                    }
                    Text {
                        attr {
                            text("切换 ›")
                            fontSize(13f)
                            color(HelixTheme.Light.primary)
                        }
                    }
                }
                // ==================== 文件夹栏 ====================
                View {
                    attr {
                        flexDirectionRow()
                        padding(8f)
                    }
                    vfor({ page.folders }) { folder ->
                        View {
                            attr {
                                marginLeft(4f)
                                marginRight(4f)
                                borderRadius(12f)
                                backgroundColor(
                                    if (page.folders.indexOf(folder) == page.selectedFolder)
                                        HelixTheme.Light.primary
                                    else HelixTheme.Light.surfaceContainerLow
                                )
                            }
                            Text {
                                attr {
                                    text(folder)
                                    fontSize(13f)
                                    color(
                                        if (page.folders.indexOf(folder) == page.selectedFolder)
                                            HelixTheme.Light.onPrimary
                                        else HelixTheme.Light.onSurfaceVariant
                                    )
                                    margin(8f)
                                }
                            }
                        }
                    }
                }
                // ==================== 会话列表 ====================
                View {
                    attr {
                        flex(1f)
                    }
                    vfor({ page.conversations }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                padding(12f)
                                marginLeft(8f)
                                marginRight(8f)
                                marginBottom(4f)
                                borderRadius(12f)
                                backgroundColor(
                                    if (item.active) HelixTheme.Light.primaryContainer
                                    else HelixTheme.Light.surface
                                )
                            }
                            event {
                                click { page.router().openPage("ChatPage") }
                            }
                            // 标题
                            Text {
                                attr {
                                    text(item.title)
                                    fontSize(14f)
                                    color(
                                        if (item.active) HelixTheme.Light.onPrimaryContainer
                                        else HelixTheme.Light.onSurface
                                    )
                                    flex(1f)
                                }
                            }
                            // 时间
                            Text {
                                attr {
                                    text(item.time)
                                    fontSize(11f)
                                    color(HelixTheme.Light.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                // ==================== 底部动作区 ====================
                View {
                    attr {
                        flexDirectionRow()
                        padding(16f)
                        backgroundColor(HelixTheme.Light.surfaceContainerLow)
                    }
                    // 新建会话
                    drawerAction(icon = "✏️", label = "新会话", page = page)
                    // 历史记录
                    drawerAction(icon = "🕘", label = "历史", page = page)
                    // 设置
                    drawerAction(icon = "⚙️", label = "设置", page = page)
                }
            }
        }
    }
}

/**
 * 抽屉底部动作按钮
 */
private fun ViewContainer<*, *>.drawerAction(icon: String, label: String, page: ChatDrawerPage) {
    View {
        attr {
            flex(1f)
            justifyContentCenter()
            alignItemsCenter()
            padding(8f)
        }
        event {
            click {
                when (label) {
                    "新会话" -> page.routerPublic().openPage("ChatPage")
                    "历史" -> page.routerPublic().openPage("HistoryPage")
                    "设置" -> page.routerPublic().openPage("SettingPage")
                }
            }
        }
        Text {
            attr {
                text(icon)
                fontSize(20f)
            }
        }
        Text {
            attr {
                text(label)
                fontSize(11f)
                color(HelixTheme.Light.onSurfaceVariant)
                marginTop(4f)
            }
        }
    }
}
