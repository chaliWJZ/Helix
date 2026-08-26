package com.helix.kuikly.pages.search

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.safeRun
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 搜索页面（对应 Compose 的 SearchPage）
 *
 * 联网搜索：搜索框 + 结果列表
 */
@Page("SearchPage")
internal class SearchPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class SearchResult(
        val title: String,
        val url: String,
        val snippet: String
    )

    private var keyword by observable("")
    private var searching by observable(false)
    private var searched by observable(false)
    private val results by observableList<SearchResult>()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "搜索",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                // 搜索框
                View {
                    attr {
                        height(48f)
                        borderRadius(24f)
                        marginLeft(16f)
                        marginRight(16f)
                        marginTop(8f)
                        paddingLeft(16f)
                        paddingRight(16f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        justifyContentCenter()
                    }
                    Input {
                        attr {
                            placeholder("输入搜索关键词...")
                            fontSize(14f)
                            flex(1f)
                        }
                        event {
                            textDidChange { ctx.keyword = it.text }
                        }
                    }
                }
                // 搜索按钮
                View {
                    attr {
                        height(44f)
                        borderRadius(22f)
                        marginLeft(16f)
                        marginRight(16f)
                        marginTop(12f)
                        justifyContentCenter()
                        alignItemsCenter()
                        backgroundColor(HelixTheme.current.primary)
                    }
                    event {
                        click {
                            safeRun("搜索页-搜索按钮") {
                                KLog.i("HelixKuikly", "👆 [Click] 搜索页-搜索按钮")
                                ctx.searching = true
                                ctx.searched = false
                                // TODO: 通过 SearchModule 执行真实搜索
                        
                            }
                        
                        }
                    }
                    Text {
                        attr {
                            text(if (ctx.searching) "搜索中..." else "搜索")
                            fontSize(15f)
                            color(HelixTheme.current.onPrimary)
                        }
                    }
                }
                // 搜索结果
                View {
                    attr {
                        flex(1f)
                        marginTop(16f)
                    }
                    vfor({ ctx.results }) { item ->
                        View {
                            attr {
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                borderRadius(16f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            Text {
                                attr {
                                    text(item.title)
                                    fontSize(15f)
                                    color(HelixTheme.current.primary)
                                }
                            }
                            Text {
                                attr {
                                    text(item.snippet)
                                    fontSize(13f)
                                    color(HelixTheme.current.onSurface)
                                    marginTop(4f)
                                }
                            }
                            Text {
                                attr {
                                    text(item.url)
                                    fontSize(11f)
                                    color(HelixTheme.current.onSurfaceVariant)
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
