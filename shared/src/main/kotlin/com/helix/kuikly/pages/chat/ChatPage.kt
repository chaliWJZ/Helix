package com.helix.kuikly.pages.chat

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.richtext.MarkdownRenderer
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 聊天页面（对应 Compose 的 ChatPage + ChatList）
 *
 * 核心聊天界面：消息列表 + 底部输入框 + 抽屉入口
 */
@Page("ChatPage")
internal class ChatPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 消息数据模型
    data class Message(
        val id: Int,
        val role: String,      // "user" / "assistant"
        val content: String,
        val time: String,
        val thinking: String? = null  // 思维链内容
    )

    // 状态（internal 供文件级组件访问）
    internal val messages by observableList<Message>()
    internal var inputText by observable("")
    internal var isGenerating by observable(false)
    internal var showDrawer by observable(false)
    private var messageCounter = 0

    override fun created() {
        super.created()
        // 欢迎消息（占位，后续接入 ChatVM Module）
        messages.add(
            Message(
                id = messageCounter++,
                role = "assistant",
                content = "你好！我是 Helix AI 助手，有什么可以帮助你的吗？",
                time = "14:30"
            )
        )
    }

    /**
     * 发送消息（占位实现，后续接入 ChatVM Module 真实 AI 回复）
     */
    fun sendMessage(text: String) {
        messages.add(
            Message(
                id = messageCounter++,
                role = "user",
                content = text,
                time = "刚刚"
            )
        )
        // 模拟 AI 回复（占位）
        isGenerating = true
        messages.add(
            Message(
                id = messageCounter++,
                role = "assistant",
                content = "收到你的消息：「$text」\n\n（这是占位回复，真实 AI 回复将在接入 ChatVM Module 后实现）",
                time = "刚刚",
                thinking = "用户发来消息，需要分析意图并给出回复..."
            )
        )
        isGenerating = false
        inputText = ""
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                // 顶部导航栏
                NavBar(
                    title = "新会话",
                    showBack = false,
                    rightActions = listOf(
                        NavBarAction(label = "☰") { ctx.showDrawer = !ctx.showDrawer },
                        NavBarAction(label = "＋") { ctx.router().openPage("ChatPage") }
                    )
                )
                // 消息列表
                View {
                    attr {
                        flex(1f)
                        marginTop(4f)
                    }
                    vfor({ ctx.messages }) { message ->
                        MessageBubble(ctx = ctx, message = message)
                    }
                }
                // 底部输入栏
                ChatInputBar(ctx = ctx)
                // 抽屉遮罩（点击关闭）
                vif({ ctx.showDrawer }) {
                    View {
                        attr {
                            positionAbsolute()
                            left(0f)
                            top(0f)
                            right(0f)
                            bottom(0f)
                            backgroundColor(0x33000000)
                        }
                        event {
                            click {
                                KLog.i("HelixKuikly", "👆 [Click] 聊天页-遮罩关闭抽屉")
                                ctx.showDrawer = false
                            }
                        }
                        // 抽屉面板（左侧）
                        View {
                            attr {
                                width(250f)
                                flex(1f)
                                backgroundColor(HelixTheme.current.surface)
                            }
                            Text {
                                attr {
                                    text("会话抽屉")
                                    fontSize(16f)
                                    color(HelixTheme.current.onSurface)
                                    margin(16f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 消息气泡组件（文件级私有扩展函数）
 */
private fun ViewContainer<*, *>.MessageBubble(ctx: ChatPage, message: ChatPage.Message) {
    View {
        attr {
            if (message.role == "user") alignSelfFlexEnd() else alignSelfFlexStart()
            maxWidth(300f)
            margin(8f)
            padding(12f)
            borderRadius(16f)
            backgroundColor(
                if (message.role == "user") HelixTheme.current.primary
                else HelixTheme.current.surfaceContainerLow
            )
        }
        // 思维链展示
        message.thinking?.let { thinking ->
            Text {
                attr {
                    text("💭 $thinking")
                    fontSize(12f)
                    color(HelixTheme.current.onSurfaceVariant)
                    marginBottom(6f)
                }
            }
        }
        // 消息内容（assistant 消息用 Markdown 渲染）
        if (message.role == "assistant") {
            MarkdownRenderer(markdown = message.content)
        } else {
            Text {
                attr {
                    text(message.content)
                    fontSize(15f)
                    color(HelixTheme.current.onPrimary)
                }
            }
        }
        // 时间戳
        Text {
            attr {
                text(message.time)
                fontSize(10f)
                color(
                    if (message.role == "user") HelixTheme.current.onPrimary
                    else HelixTheme.current.onSurfaceVariant
                )
                marginTop(4f)
            }
        }
    }
}

/**
 * 底部输入栏组件
 */
private fun ViewContainer<*, *>.ChatInputBar(ctx: ChatPage) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            padding(8f)
            backgroundColor(HelixTheme.current.surface)
        }
        // 输入框
        View {
            attr {
                flex(1f)
                minHeight(40f)
                borderRadius(20f)
                marginLeft(16f)
                backgroundColor(HelixTheme.current.surfaceContainerLow)
                justifyContentCenter()
            }
            Input {
                attr {
                    placeholder("输入消息...")
                    fontSize(14f)
                    flex(1f)
                }
                event {
                    textDidChange { ctx.inputText = it.text }
                }
            }
        }
        // 发送按钮
        View {
            attr {
                width(40f)
                height(40f)
                borderRadius(20f)
                marginLeft(8f)
                marginRight(16f)
                justifyContentCenter()
                alignItemsCenter()
                backgroundColor(
                    if (ctx.inputText.isNotEmpty()) HelixTheme.current.primary
                    else HelixTheme.current.surfaceContainerHighest
                )
            }
            event {
                click {
                    KLog.i("HelixKuikly", "👆 [Click] 聊天页-发送按钮")
                    if (ctx.inputText.isNotEmpty() && !ctx.isGenerating) {
                        ctx.sendMessage(ctx.inputText)
                    }
                }
            }
            Text {
                attr {
                    text("↑")
                    fontSize(18f)
                    color(HelixTheme.current.onPrimary)
                }
            }
        }
    }
}
