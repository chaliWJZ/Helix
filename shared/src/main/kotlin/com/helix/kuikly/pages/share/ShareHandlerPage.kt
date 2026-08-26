package com.helix.kuikly.pages.share

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 分享处理页面（对应 Compose 的 ShareHandlerPage）
 *
 * 处理外部分享进来的文本/图片：预览 + 选择会话发送
 */
@Page("ShareHandlerPage")
internal class ShareHandlerPage : HelixBasePage() {

    private var sharedText by observable("（分享内容待接收）")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "接收分享",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                    }
                    // 分享内容预览
                    View {
                        attr {
                            padding(16f)
                            marginLeft(16f)
                            marginRight(16f)
                            marginTop(8f)
                            marginBottom(12f)
                            borderRadius(16f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        Text {
                            attr {
                                text("分享内容"
                                )
                                fontSize(14f)
                                fontWeight700()
                                color(HelixTheme.current.onSurface)
                                marginBottom(8f)
                            }
                        }
                        Text {
                            attr {
                                text(ctx.sharedText)
                                fontSize(14f)
                                color(HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                    // 发送到新会话按钮
                    View {
                        attr {
                            height(44f)
                            borderRadius(22f)
                            marginLeft(16f)
                            marginRight(16f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(HelixTheme.current.primary)
                        }
                        event {
                            click {
                                safeRun("[ShareHandlerPage#body] 分享处理页-发送到新会话") {
                                    KLog.i("HelixKuikly", "👆 [Click] [ShareHandlerPage#body] 分享处理页-发送到新会话")
                                    ctx.openPage("ChatPage")
                            
                                }
                            
                            }
                        }
                        Text {
                            attr {
                                text("发送到新会话")
                                fontSize(15f)
                                color(HelixTheme.current.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
