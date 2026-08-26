package com.helix.kuikly.pages.imggen

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 图片生成页面（对应 Compose 的 ImgGenPage）
 *
 * AI 画图：提示词输入 + 生成结果展示
 */
@Page("ImgGenPage")
internal class ImgGenPage : HelixBasePage() {

    private var prompt by observable("")
    private var generating by observable(false)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "图片生成",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                    }
                    // 提示词输入
                    View {
                        attr {
                            height(100f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginTop(8f)
                            marginBottom(12f)
                            padding(12f)
                        }
                        Input {
                            attr {
                                placeholder("描述你想生成的图片...")
                                fontSize(15f)
                                flex(1f)
                            }
                            event {
                                textDidChange { ctx.prompt = it.text }
                            }
                        }
                    }
                    // 生成按钮
                    View {
                        attr {
                            height(44f)
                            borderRadius(22f)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(16f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(HelixTheme.current.primary)
                        }
                        event {
                            click {
                                KLog.i("HelixKuikly", "👆 [Click] 图片生成页-生成按钮")
                                if (ctx.prompt.isNotEmpty()) {
                                    ctx.generating = true
                                    // TODO: 接入图片生成 Module
                                }
                            }
                        }
                        Text {
                            attr {
                                text(if (ctx.generating) "生成中..." else "生成图片")
                                fontSize(15f)
                                color(HelixTheme.current.onPrimary)
                            }
                        }
                    }
                    // 结果展示区
                    View {
                        attr {
                            flex(1f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(16f)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text(if (ctx.generating) "🎨" else "🖼️")
                                fontSize(48f)
                            }
                        }
                        Text {
                            attr {
                                text(
                                    if (ctx.generating) "正在生成图片..."
                                    else "生成的图片将显示在这里"
                                )
                                fontSize(13f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                            }
                        }
                    }
                }
            }
        }
    }
}
