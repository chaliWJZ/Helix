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
import com.helix.kuikly.theme.HelixTheme

/**
 * 快捷消息页面（对应 Compose 的 QuickMessagesPage）
 *
 * 快捷消息管理：列表 + 添加
 */
@Page("QuickMessagesPage")
internal class QuickMessagesPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private val messages by observableList<String>()

    override fun created() {
        super.created()
        listOf(
            "继续",
            "给我更多细节",
            "用更简单的方式解释",
            "翻译成英文",
            "总结上面的内容",
            "列一个大纲",
            "有哪些风险？",
            "举个实际例子"
        ).forEach { messages.add(it) }
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
                    title = "快捷消息",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ ctx.messages }) { item ->
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
                            Text {
                                attr {
                                    text(item)
                                    fontSize(15f)
                                    color(HelixTheme.current.onSurface)
                                    flex(1f)
                                }
                            }
                            Text {
                                attr {
                                    text("✕")
                                    fontSize(14f)
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
