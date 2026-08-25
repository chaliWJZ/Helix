package com.helix.kuikly.components.richtext

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

/**
 * Markdown 渲染组件（Kuikly 实现）
 *
 * 渲染 MarkdownParser 解析出的元素：
 * - 标题 → 大字号 Text
 * - 代码块 → 深色背景 + 等宽字体
 * - 列表 → 圆点/序号 + 缩进
 * - 引用 → 左侧竖线 + 灰色文字
 * - 分割线 → 细线 View
 */
fun ViewContainer<*, *>.MarkdownRenderer(markdown: String) {
    val elements = MarkdownParser.parse(markdown)
    View {
        elements.forEach { element ->
            when (element) {
                is MarkdownParser.MdElement.Heading -> renderHeading(element)
                is MarkdownParser.MdElement.Paragraph -> renderParagraph(element)
                is MarkdownParser.MdElement.CodeBlock -> renderCodeBlock(element)
                is MarkdownParser.MdElement.ListItem -> renderListItem(element)
                is MarkdownParser.MdElement.Quote -> renderQuote(element)
                is MarkdownParser.MdElement.Divider -> renderDivider()
            }
        }
    }
}

/** 渲染标题 */
private fun ViewContainer<*, *>.renderHeading(element: MarkdownParser.MdElement.Heading) {
    Text {
        attr {
            text(element.text)
            fontSize(
                when (element.level) {
                    1 -> 24f
                    2 -> 20f
                    3 -> 18f
                    4 -> 16f
                    else -> 15f
                }
            )
            fontWeight700()
            color(HelixTheme.current.onSurface)
            marginTop(12f)
            marginBottom(6f)
        }
    }
}

/** 渲染段落 */
private fun ViewContainer<*, *>.renderParagraph(element: MarkdownParser.MdElement.Paragraph) {
    Text {
        attr {
            text(element.text)
            fontSize(15f)
            color(HelixTheme.current.onSurface)
            marginTop(6f)
            marginBottom(6f)
        }
    }
}

/** 渲染代码块 */
private fun ViewContainer<*, *>.renderCodeBlock(element: MarkdownParser.MdElement.CodeBlock) {
    View {
        attr {
            borderRadius(12f)
            backgroundColor(Color(0xFF1E1E2EL)) // 深色背景
            marginTop(8f)
            marginBottom(8f)
            padding(12f)
            alignSelfStretch()
        }
        // 语言标签
        if (element.language.isNotEmpty()) {
            Text {
                attr {
                    text(element.language)
                    fontSize(11f)
                    color(Color(0xFF89B4FAL))
                    marginBottom(4f)
                }
            }
        }
        // 代码内容
        Text {
            attr {
                text(element.code)
                fontSize(13f)
                color(Color(0xFFCDD6F4L))
                fontFamily("monospace")
            }
        }
    }
}

/** 渲染列表项 */
private fun ViewContainer<*, *>.renderListItem(element: MarkdownParser.MdElement.ListItem) {
    View {
        attr {
            flexDirectionRow()
            marginTop(4f)
            marginBottom(4f)
            paddingLeft(8f)
            paddingRight(8f)
            alignSelfStretch()
        }
        // 圆点或序号
        Text {
            attr {
                text(
                    if (element.ordered) "${element.index}. "
                    else "• "
                )
                fontSize(15f)
                color(HelixTheme.current.primary)
                marginRight(6f)
            }
        }
        // 内容
        Text {
            attr {
                text(element.text)
                fontSize(15f)
                color(HelixTheme.current.onSurface)
                flex(1f)
            }
        }
    }
}

/** 渲染引用 */
private fun ViewContainer<*, *>.renderQuote(element: MarkdownParser.MdElement.Quote) {
    View {
        attr {
            flexDirectionRow()
            marginTop(8f)
            marginBottom(8f)
            alignSelfStretch()
        }
        // 左侧竖线
        View {
            attr {
                width(3f)
                borderRadius(2f)
                backgroundColor(HelixTheme.current.primaryContainer)
                marginRight(10f)
            }
        }
        // 引用文字
        Text {
            attr {
                text(element.text)
                fontSize(14f)
                color(HelixTheme.current.onSurfaceVariant)
                flex(1f)
            }
        }
    }
}

/** 渲染分割线 */
private fun ViewContainer<*, *>.renderDivider() {
    View {
        attr {
            height(1f)
            backgroundColor(HelixTheme.current.outlineVariant)
            marginTop(12f)
            marginBottom(12f)
            alignSelfStretch()
        }
    }
}
