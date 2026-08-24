package com.helix.kuikly.pages.log

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
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
 * 日志页面（对应 Compose 的 LogPage）
 *
 * 应用日志查看器：级别过滤 + 日志列表
 */
@Page("LogPage")
internal class LogPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class LogEntry(
        val level: String,
        val tag: String,
        val message: String,
        val time: String
    )

    private val logs by observableList<LogEntry>()

    override fun created() {
        super.created()
        listOf(
            LogEntry("INFO", "AppStartup", "应用启动完成，耗时 1.2s", "22:30:01"),
            LogEntry("DEBUG", "ChatVM", "加载会话列表成功，共 12 个会话", "22:30:02"),
            LogEntry("INFO", "NetworkModule", "请求模型列表: openai/gpt-4o", "22:30:15"),
            LogEntry("WARN", "NetworkModule", "请求超时，正在重试 (1/3)", "22:30:25"),
            LogEntry("ERROR", "TTSModule", "TTS 引擎初始化失败: 未安装", "22:31:00"),
            LogEntry("INFO", "BackupVM", "自动备份完成", "22:35:00")
        ).forEach { logs.add(it) }
    }

    private fun levelColor(level: String) = when (level) {
        "ERROR" -> Color(0xFFE53935L)
        "WARN" -> Color(0xFFFB8C00L)
        "DEBUG" -> Color(0xFF43A047L)
        else -> Color(0xFF1E88E5L)
    }

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "日志",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ page.logs }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                padding(12f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(1f)
                                backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            }
                            // 级别
                            Text {
                                attr {
                                    text(item.level)
                                    fontSize(10f)
                                    color(page.levelColor(item.level))
                                    fontWeight700()
                                }
                            }
                            // 内容
                            View {
                                attr {
                                    flex(1f)
                                    marginLeft(8f)
                                }
                                Text {
                                    attr {
                                        text("[${item.tag}] ${item.message}")
                                        fontSize(12f)
                                        color(HelixTheme.Light.onSurface)
                                    }
                                }
                            }
                            // 时间
                            Text {
                                attr {
                                    text(item.time)
                                    fontSize(10f)
                                    color(HelixTheme.Light.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
