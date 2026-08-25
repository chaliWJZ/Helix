package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * MCP 设置页面（对应 Compose 的 SettingMcpPage，原1102行）
 *
 * MCP 服务器管理：服务器列表 + 启停状态
 */
@Page("SettingMcpPage")
internal class SettingMcpPage : HelixBasePage() {

    data class McpServer(
        val name: String,
        val command: String,
        val enabled: Boolean,
        val tools: Int
    )

    private val servers by observableList<McpServer>()

    override fun created() {
        super.created()
        listOf(
            McpServer("filesystem", "npx @modelcontextprotocol/server-filesystem", true, 11),
            McpServer("fetch", "uvx mcp-server-fetch", true, 1),
            McpServer("memory", "npx @modelcontextprotocol/server-memory", false, 9),
            McpServer("git", "uvx mcp-server-git", true, 7)
        ).forEach { servers.add(it) }
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
                    title = "MCP",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "＋") { ctx.bridge().toast("添加 MCP 服务器（待接入）") }
                    )
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ ctx.servers }) { server ->
                        View {
                            attr {
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                borderRadius(16f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                }
                                Text {
                                    attr {
                                        text(server.name)
                                        fontSize(16f)
                                        color(HelixTheme.current.onSurface)
                                        flex(1f)
                                    }
                                }
                                // 启停状态
                                Text {
                                    attr {
                                        text(if (server.enabled) "● 已启用" else "○ 已禁用")
                                        fontSize(12f)
                                        color(
                                            if (server.enabled) HelixTheme.current.primary
                                            else HelixTheme.current.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Text {
                                attr {
                                    text(server.command)
                                    fontSize(12f)
                                    color(HelixTheme.current.onSurfaceVariant)
                                    marginTop(4f)
                                    fontFamily("monospace")
                                }
                            }
                            Text {
                                attr {
                                    text("${server.tools} 个工具可用")
                                    fontSize(11f)
                                    color(HelixTheme.current.primary)
                                    marginTop(4f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
