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
                // NavBar 右侧「清空」按钮需随 fileItems 是否为空动态显隐。
                // rightActions 是普通 List（非响应式 lambda），若直接用
                // `if (ctx.fileItems.isNotEmpty()) listOf(...) else emptyList()`
                // 计算，只会在首次 body() 构建时求值一次，之后 fileItems 变化
                // 不会重新计算——按钮清空后仍会残留显示（测试发现的真实bug）。
                // 用 vif 包裹两种 NavBar 分支，vif 已验证响应式生效。
                vif({ ctx.fileItems.isNotEmpty() }) {
                    NavBar(
                        title = "文件管理",
                        showBack = true,
                        onBack = { ctx.router().closePage() },
                        rightActions = listOf(NavBarAction(label = "清空") { ctx.fileItems = emptyList() })
                    )
                }
                vif({ ctx.fileItems.isEmpty() }) {
                    NavBar(
                        title = "文件管理",
                        showBack = true,
                        onBack = { ctx.router().closePage() }
                    )
                }
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
