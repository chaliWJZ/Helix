package com.helix.kuikly.pages.stats

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 统计页面（对应 Compose 的 StatsPage）
 *
 * 使用统计：消息数/Token消耗/会话数等
 */
@Page("StatsPage")
internal class StatsPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "统计",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 统计卡片网格
                    View {
                        attr {
                            flexDirectionRow()
                            flexWrapWrap()
                            marginLeft(8f)
                            marginRight(8f)
                        }
                        // 统计项
                        statCard("💬", "156", "总消息数")
                        statCard("🗂️", "23", "总会话数")
                        statCard("🔤", "48.2K", "总 Token 消耗")
                        statCard("🤖", "5", "使用的模型数")
                        statCard("📅", "7", "活跃天数")
                        statCard("❤️", "12", "收藏消息数")
                    }
                }
            }
        }
    }
}

/**
 * 统计卡片组件（文件级私有扩展函数，避免 ViewBuilder 内隐式 receiver 问题）
 */
private fun ViewContainer<*, *>.statCard(icon: String, value: String, label: String) {
    View {
        attr {
            width(150f)
            height(100f)
            margin(8f)
            borderRadius(20f)
            backgroundColor(HelixTheme.current.surfaceContainerLow)
            justifyContentCenter()
            alignItemsCenter()
        }
        Text {
            attr {
                text(icon)
                fontSize(24f)
            }
        }
        Text {
            attr {
                text(value)
                fontSize(24f)
                fontWeight700()
                color(HelixTheme.current.onSurface)
                marginTop(4f)
            }
        }
        Text {
            attr {
                text(label)
                fontSize(12f)
                color(HelixTheme.current.onSurfaceVariant)
                marginTop(2f)
            }
        }
    }
}
