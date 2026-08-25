package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 主题设置页面（对应 Compose 的 SettingThemePage）
 *
 * 主题色选择 + 主题模式
 */
@Page("SettingThemePage")
internal class SettingThemePage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 预设主题色
    private val themeColors = listOf(
        "默认蓝" to 0xFF1976D2,
        "紫色" to 0xFF7C4DFF,
        "绿色" to 0xFF388E3C,
        "橙色" to 0xFFF57C00,
        "粉色" to 0xFFE91E63,
        "青色" to 0xFF0097A7
    )

    private var selectedColorIndex by observable(0)
    private var colorMode by observable(0) // 0=系统 1=亮色 2=暗色

    override fun body(): ViewBuilder {
        val page = this
        val colors = themeColors
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "主题",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 主题模式选择
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "颜色模式",
                                subtitle = when (page.colorMode) {
                                    1 -> "亮色"
                                    2 -> "暗色"
                                    else -> "跟随系统"
                                },
                                icon = "🌓",
                                onClick = { page.colorMode = (page.colorMode + 1) % 3 }
                            )
                        )
                    )
                    // 主题色选择标题
                    Text {
                        attr {
                            text("主题色")
                            fontSize(14f)
                            color(HelixTheme.current.primary)
                            marginLeft(16f)
                            marginTop(16f)
                            marginBottom(8f)
                        }
                    }
                    // 主题色网格
                    View {
                        attr {
                            flexDirectionRow()
                            flexWrapWrap()
                            marginLeft(16f)
                            marginRight(16f)
                        }
                        colors.forEachIndexed { index, (name, color) ->
                            View {
                                attr {
                                    width(48f)
                                    height(48f)
                                    borderRadius(24f)
                                    backgroundColor(color)
                                    margin(8f)
                                    justifyContentCenter()
                                    alignItemsCenter()
                                    border(Border(2f, BorderStyle.SOLID, HelixTheme.current.onSurface))
                                }
                                event {
                                    click { page.selectedColorIndex = index }
                                }
                                Text {
                                    attr {
                                        text(if (page.selectedColorIndex == index) "✓" else "")
                                        fontSize(16f)
                                        color(0xFFFFFFFF)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
