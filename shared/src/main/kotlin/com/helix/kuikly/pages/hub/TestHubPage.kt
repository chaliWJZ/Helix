package com.helix.kuikly.pages.hub

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.nav.KuiklyNav
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 测试导航中枢页（App 首页）
 *
 * 按分类列出全部 Kuikly 页面入口，点击直达：
 * - 聊天系 / 设置系 / 列表系 / 功能系 / 工作区系
 * - Compose 版对比入口（跳原生 Compose 调试页）
 */
@Page("TestHubPage")
internal class TestHubPage : HelixBasePage() {

    /** 分类数据：emoji + 分类名 + 页面列表 */
    data class Category(val emoji: String, val name: String, val pages: List<String>)

    private val categories by observableList<Category>()
    private var openedCount by observable(0)

    override fun created() {
        super.created()
        KuiklyNav.CATEGORIES.forEach { (emoji, name, pages) ->
            categories.add(Category(emoji, name, pages))
        }
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
                    title = "Helix Kuikly 测试中枢",
                    showBack = false
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ===== Compose 对比入口 =====
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            margin(16f)
                            padding(16f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.primaryContainer)
                        }
                        event {
                            click {
                                // 跳转 Compose 版主界面（原生 DebugPage，从那里进 Compose 各页面）
                                ctx.bridge().openUrl("helix://compose-debug")
                            }
                        }
                        Text {
                            attr {
                                text("📱")
                                fontSize(28f)
                                marginRight(12f)
                            }
                        }
                        View {
                            attr {
                                flex(1f)
                            }
                            Text {
                                attr {
                                    text("Compose 版对比入口")
                                    fontSize(16f)
                                    fontWeight700()
                                    color(HelixTheme.current.onPrimaryContainer)
                                }
                            }
                            Text {
                                attr {
                                    text("跳转到 Compose 版调试页，对比原版 UI 效果")
                                    fontSize(12f)
                                    color(HelixTheme.current.onPrimaryContainer)
                                    marginTop(2f)
                                }
                            }
                        }
                        Text {
                            attr {
                                text("›")
                                fontSize(20f)
                                color(HelixTheme.current.onPrimaryContainer)
                            }
                        }
                    }

                    // ===== 各分类页面入口（vfor 闭包必须单根节点，包一层 View）=====
                    vfor({ ctx.categories }) { category ->
                        View {
                            attr {
                                flexDirectionColumn()
                                alignSelfStretch()
                            }
                            // 分类标题
                            Text {
                                attr {
                                    text("${category.emoji} ${category.name}（${category.pages.size}页）")
                                    fontSize(14f)
                                    fontWeight700()
                                    color(HelixTheme.current.primary)
                                    marginLeft(16f)
                                    marginTop(12f)
                                    marginBottom(4f)
                                }
                            }
                            // 页面项（静态数据直接遍历渲染）
                            category.pages.forEach { pageName ->
                                View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    marginLeft(16f)
                                    marginRight(16f)
                                    marginBottom(4f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                event {
                                    click {
                                        ctx.openedCount++
                                        KuiklyNav.open(pageName)
                                    }
                                }
                                // 页面名（英文）
                                Text {
                                    attr {
                                        text(pageName)
                                        fontSize(13f)
                                        fontFamily("monospace")
                                        color(HelixTheme.current.onSurface)
                                        flex(1f)
                                    }
                                }
                                // 中文标题
                                Text {
                                    attr {
                                        text(KuiklyNav.title(pageName))
                                        fontSize(14f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginRight(8f)
                                    }
                                }
                                Text {
                                    attr {
                                        text("›")
                                        fontSize(16f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        }
                    }

                    // 底部说明
                    Text {
                        attr {
                            text("已打开 ${ctx.openedCount} 次 | 共 ${KuiklyNav.PAGES.size} 个页面\n点左上角/返回键逐级返回")
                            fontSize(11f)
                            color(HelixTheme.current.onSurfaceVariant)
                            marginTop(16f)
                            marginBottom(32f)
                            alignSelfCenter()
                        }
                    }
                }
            }
        }
    }
}
