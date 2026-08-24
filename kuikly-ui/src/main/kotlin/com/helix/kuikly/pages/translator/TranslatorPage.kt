package com.helix.kuikly.pages.translator

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 翻译页面（对应 Compose 的 TranslatorPage）
 *
 * 文本翻译：源语言/目标语言选择 + 输入输出
 */
@Page("TranslatorPage")
internal class TranslatorPage : HelixBasePage() {

    private var sourceText by observable("")
    private var resultText by observable("")
    private var translating by observable(false)
    private var sourceLang by observable("自动检测")
    private var targetLang by observable("英语")

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "翻译",
                    showBack = true,
                    onBack = { page.closePage() }
                )
                // 语言选择栏
                View {
                    attr {
                        flexDirectionRow()
                        justifyContentCenter()
                        alignItemsCenter()
                        height(44f)
                    }
                    Text {
                        attr {
                            text(page.sourceLang)
                            fontSize(14f)
                            color(HelixTheme.Light.primary)
                            margin(8f)
                        }
                    }
                    Text {
                        attr {
                            text("⇄")
                            fontSize(16f)
                            color(HelixTheme.Light.onSurfaceVariant)
                            margin(8f)
                        }
                    }
                    Text {
                        attr {
                            text(page.targetLang)
                            fontSize(14f)
                            color(HelixTheme.Light.primary)
                            margin(8f)
                        }
                    }
                }
                // 输入区
                View {
                    attr {
                        height(150f)
                        borderRadius(16f)
                        backgroundColor(HelixTheme.Light.surfaceContainerLow)
                        marginLeft(16f)
                        marginRight(16f)
                        marginBottom(12f)
                        padding(12f)
                    }
                    Input {
                        attr {
                            placeholder("输入要翻译的文本...")
                            fontSize(15f)
                            flex(1f)
                        }
                        event {
                            textDidChange { page.sourceText = it.text }
                        }
                    }
                }
                // 翻译按钮
                View {
                    attr {
                        height(44f)
                        borderRadius(22f)
                        marginLeft(16f)
                        marginRight(16f)
                        marginBottom(12f)
                        justifyContentCenter()
                        alignItemsCenter()
                        backgroundColor(HelixTheme.Light.primary)
                    }
                    event {
                        click {
                            page.translating = true
                            // TODO: 接入翻译 Module
                            page.resultText = "（翻译功能待接入 AI Module）"
                            page.translating = false
                        }
                    }
                    Text {
                        attr {
                            text(if (page.translating) "翻译中..." else "翻译")
                            fontSize(15f)
                            color(HelixTheme.Light.onPrimary)
                        }
                    }
                }
                // 结果区
                View {
                    attr {
                        flex(1f)
                        borderRadius(16f)
                        backgroundColor(HelixTheme.Light.surfaceContainerLow)
                        marginLeft(16f)
                        marginRight(16f)
                        marginBottom(16f)
                        padding(12f)
                    }
                    Text {
                        attr {
                            text(if (page.resultText.isEmpty()) "翻译结果将显示在这里" else page.resultText)
                            fontSize(15f)
                            color(
                                if (page.resultText.isEmpty()) HelixTheme.Light.onSurfaceVariant
                                else HelixTheme.Light.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
