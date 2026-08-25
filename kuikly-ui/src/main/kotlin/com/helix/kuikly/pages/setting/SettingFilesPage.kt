package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.theme.HelixTheme

/**
 * 文件管理页面（对应 Compose 的 SettingFilesPage）
 *
 * 上传文件夹管理：文件列表 + 删除/清空功能
 */
@Page("SettingFilesPage")
internal class SettingFilesPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 文件列表（占位数据，后续接入 FilesModule）
    private var fileItems by observable(
        listOf(
            CardGroupItem(title = "image_001.png", subtitle = "1.2 MB · 2026-08-20", icon = "🖼️"),
            CardGroupItem(title = "document.pdf", subtitle = "340 KB · 2026-08-21", icon = "📄"),
            CardGroupItem(title = "screenshot.jpg", subtitle = "890 KB · 2026-08-22", icon = "📷")
        )
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
                    title = "文件管理",
                    showBack = true,
                    onBack = { ctx.router().closePage() },
                    rightActions = if (ctx.fileItems.isNotEmpty()) {
                        listOf(NavBarAction(label = "清空") { ctx.fileItems = emptyList() })
                    } else {
                        emptyList()
                    }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    // 空状态
                    vif({ ctx.fileItems.isEmpty() }) {
                        Text {
                            attr {
                                text("📁")
                                fontSize(48f)
                            }
                        }
                        Text {
                            attr {
                                text("暂无文件")
                                fontSize(14f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                            }
                        }
                    }
                    // 文件列表
                    vif({ ctx.fileItems.isNotEmpty() }) {
                        CardGroup(items = ctx.fileItems)
                    }
                }
                // 统计信息
                View {
                    attr {
                        height(48f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text("共 ${ctx.fileItems.size} 个文件")
                            fontSize(12f)
                            color(HelixTheme.current.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
