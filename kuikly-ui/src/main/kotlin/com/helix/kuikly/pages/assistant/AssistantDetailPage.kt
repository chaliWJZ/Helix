package com.helix.kuikly.pages.assistant

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 助手详情页面（对应 Compose 的 AssistantDetailPage + 子页面）
 *
 * Tab 式详情：基础信息/提示词/记忆/MCP/扩展/请求参数
 */
@Page("AssistantDetailPage")
internal class AssistantDetailPage : HelixBasePage() {

    private val tabs by observableList<String>()
    private var currentTab by observable(0)

    override fun created() {
        super.created()
        listOf("基础", "提示词", "记忆", "MCP", "扩展", "请求").forEach { tabs.add(it) }
    }

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "编辑助手",
                    showBack = true,
                    onBack = { page.closePage() }
                )
                // 助手头部
                View {
                    attr {
                        flexDirectionRow()
                        alignItemsCenter()
                        padding(16f)
                    }
                    View {
                        attr {
                            width(56f)
                            height(56f)
                            borderRadius(28f)
                            backgroundColor(HelixTheme.current.primaryContainer)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("🤖")
                                fontSize(28f)
                            }
                        }
                    }
                    View {
                        attr {
                            flex(1f)
                            marginLeft(12f)
                        }
                        Text {
                            attr {
                                text("默认助手")
                                fontSize(18f)
                                color(HelixTheme.current.onSurface)
                            }
                        }
                        Text {
                            attr {
                                text("通用的 AI 聊天助手")
                                fontSize(13f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(2f)
                            }
                        }
                    }
                }
                // Tab 栏
                View {
                    attr {
                        flexDirectionRow()
                        marginLeft(16f)
                        marginRight(16f)
                        marginBottom(8f)
                        borderRadius(12f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        padding(4f)
                    }
                    vfor({ page.tabs }) { tab ->
                        View {
                            attr {
                                flex(1f)
                                height(32f)
                                borderRadius(10f)
                                justifyContentCenter()
                                alignItemsCenter()
                                backgroundColor(
                                    if (page.tabs.indexOf(tab) == page.currentTab)
                                        HelixTheme.current.primary
                                    else HelixTheme.current.surfaceContainerLow
                                )
                            }
                            event {
                                click { page.currentTab = page.tabs.indexOf(tab) }
                            }
                            Text {
                                attr {
                                    text(tab)
                                    fontSize(12f)
                                    color(
                                        if (page.tabs.indexOf(tab) == page.currentTab)
                                            HelixTheme.current.onPrimary
                                        else HelixTheme.current.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                // 内容区（按 Tab 显示占位说明）
                View {
                    attr {
                        flex(1f)
                        borderRadius(16f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        marginLeft(16f)
                        marginRight(16f)
                        marginBottom(16f)
                        padding(16f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text(
                                when (page.currentTab) {
                                    0 -> "基础信息：名称/头像/描述编辑\n（待接入 AssistantVM）"
                                    1 -> "提示词编辑\n（待接入 AssistantVM）"
                                    2 -> "助手记忆管理\n（待接入 AssistantVM）"
                                    3 -> "MCP 服务器配置\n（待接入 AssistantVM）"
                                    4 -> "扩展能力配置\n（待接入 AssistantVM）"
                                    else -> "请求参数覆盖\n（待接入 AssistantVM）"
                                }
                            )
                            fontSize(14f)
                            color(HelixTheme.current.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
