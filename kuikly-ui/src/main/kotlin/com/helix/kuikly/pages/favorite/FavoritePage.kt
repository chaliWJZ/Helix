package com.helix.kuikly.pages.favorite

import com.tencent.kuikly.core.annotations.Page
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
 * 收藏页面（对应 Compose 的 FavoritePage）
 *
 * 收藏的消息列表
 */
@Page("FavoritePage")
internal class FavoritePage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class FavoriteMessage(
        val id: String,
        val content: String,
        val conversationTitle: String,
        val time: String
    )

    private val favorites by observableList<FavoriteMessage>()

    override fun created() {
        super.created()
        listOf(
            FavoriteMessage("1", "协程的取消需要配合检查 isActive 或 ensureActive()...", "关于 Kotlin 协程的问题", "今天"),
            FavoriteMessage("2", "使用 rememberSaveable 可以在配置变更时保存状态...", "Compose 学习笔记", "昨天"),
            FavoriteMessage("3", "Flow 的 cold 特性意味着每个收集者都会触发新的执行...", "Flow 深入理解", "8月22日")
        ).forEach { favorites.add(it) }
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
                    title = "收藏",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ ctx.favorites }) { item ->
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
                                    text(item.content)
                                    fontSize(14f)
                                    color(HelixTheme.current.onSurface)
                                }
                            }
                            View {
                                attr {
                                    flexDirectionRow()
                                    marginTop(8f)
                                }
                                Text {
                                    attr {
                                        text("来自：${item.conversationTitle}")
                                        fontSize(12f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        flex(1f)
                                    }
                                }
                                Text {
                                    attr {
                                        text(item.time)
                                        fontSize(12f)
                                        color(HelixTheme.current.onSurfaceVariant)
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
