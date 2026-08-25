package com.helix.kuikly.pages.backup

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 备份页面（对应 Compose 的 BackupPage）
 *
 * 数据备份/恢复：WebDAV + 本地备份
 */
@Page("BackupPage")
internal class BackupPage : HelixBasePage() {

    private var lastBackupTime by observable("从未备份")
    private var backingUp by observable(false)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "备份",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 备份状态
                    View {
                        attr {
                            borderRadius(20f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(16f)
                            padding(16f)
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text(if (ctx.backingUp) "备份中..." else "💾")
                                fontSize(40f)
                            }
                        }
                        Text {
                            attr {
                                text("上次备份：${ctx.lastBackupTime}")
                                fontSize(14f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                            }
                        }
                    }
                    // 操作卡片
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "立即备份",
                                subtitle = "备份会话/设置/供应商配置到本地",
                                icon = "⬆️",
                                onClick = { ctx.bridge().toast("开始备份（待接入）") }
                            ),
                            CardGroupItem(
                                title = "恢复备份",
                                subtitle = "从备份文件恢复数据",
                                icon = "⬇️",
                                onClick = { ctx.bridge().toast("恢复功能（待接入）") }
                            ),
                            CardGroupItem(
                                title = "WebDAV 设置",
                                subtitle = "配置远程 WebDAV 服务器",
                                icon = "☁️",
                                onClick = { ctx.bridge().toast("WebDAV 配置（待接入）") }
                            ),
                            CardGroupItem(
                                title = "自动备份",
                                subtitle = "每天自动备份到 WebDAV",
                                icon = "🕐",
                                onClick = { ctx.bridge().toast("自动备份设置（待接入）") }
                            )
                        )
                    )
                }
            }
        }
    }
}
