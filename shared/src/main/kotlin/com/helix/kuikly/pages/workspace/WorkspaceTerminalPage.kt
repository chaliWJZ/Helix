package com.helix.kuikly.pages.workspace

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 工作区终端页面（对应 Compose 的 WorkspaceTerminalPage）
 *
 * Termux 终端桥接：终端输出展示 + 命令输入
 * （原生终端通过 TerminalModule 桥接，此处为 Kuikly 侧 UI）
 */
@Page("WorkspaceTerminalPage")
internal class WorkspaceTerminalPage : HelixBasePage() {

    private var terminalOutput by com.tencent.kuikly.core.reactive.handler.observable(
        "\$ 欢迎使用 Helix 终端\n\$ （Termux 终端正在通过 Module 桥接加载...）\n\$ "
    )

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "终端",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                // 终端输出区（黑底绿字）
                View {
                    attr {
                        flex(1f)
                        backgroundColor(HelixTheme.Dark.background)
                        margin(8f)
                        borderRadius(12f)
                        padding(12f)
                    }
                    Text {
                        attr {
                            text(ctx.terminalOutput)
                            fontSize(13f)
                            color(HelixTheme.Dark.primary)
                            fontFamily("monospace")
                        }
                    }
                }
                // 命令输入提示
                View {
                    attr {
                        height(44f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text("终端输入由原生 Termux 层处理（Module 桥接）")
                            fontSize(12f)
                            color(HelixTheme.current.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
