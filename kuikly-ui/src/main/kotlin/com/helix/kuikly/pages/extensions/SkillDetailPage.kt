package com.helix.kuikly.pages.extensions

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 技能详情页面（对应 Compose 的 SkillDetailPage）
 *
 * Agent 技能详情：描述/参数/启用状态
 */
@Page("SkillDetailPage")
internal class SkillDetailPage : HelixBasePage() {

    private var skillName by observable("web_search")
    private var skillDesc by observable("联网搜索最新信息，支持指定搜索数量和安全级别")
    private var enabled by observable(true)

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "技能详情",
                    showBack = true,
                    onBack = { page.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 技能头部
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            padding(16f)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(8f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        Text {
                            attr {
                                text("🔎")
                                fontSize(32f)
                                marginRight(12f)
                            }
                        }
                        View {
                            attr {
                                flex(1f)
                            }
                            Text {
                                attr {
                                    text(page.skillName)
                                    fontSize(17f)
                                    color(HelixTheme.current.onSurface)
                                }
                            }
                            Text {
                                attr {
                                    text(page.skillDesc)
                                    fontSize(13f)
                                    color(HelixTheme.current.onSurfaceVariant)
                                    marginTop(2f)
                                }
                            }
                        }
                    }
                    // 参数说明
                    View {
                        attr {
                            padding(16f)
                            marginLeft(16f)
                            marginRight(16f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        Text {
                            attr {
                                text("参数"
                                )
                                fontSize(14f)
                                fontWeight700()
                                color(HelixTheme.current.onSurface)
                                marginBottom(8f)
                            }
                        }
                        Text {
                            attr {
                                text("• query: string（必填）搜索关键词\n• limit: int 搜索结果数量，默认 5\n• safe: bool 安全搜索，默认 true")
                                fontSize(13f)
                                color(HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
