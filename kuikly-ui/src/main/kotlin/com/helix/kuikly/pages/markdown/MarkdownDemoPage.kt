package com.helix.kuikly.pages.markdown

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.richtext.MarkdownRenderer
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * Markdown 演示页面
 *
 * 展示 Kuikly 版 Markdown 渲染器的全部能力
 */
@Page("MarkdownDemoPage")
internal class MarkdownDemoPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    private val demoMarkdown = """
        # Helix Markdown 渲染器

        这是一个 **Kuikly** 实现的 Markdown 渲染组件，支持常见元素。

        ## 功能列表

        - 标题渲染（H1-H6）
        - 段落文字
        - 代码块高亮
        - 有序和无序列表
        - 引用块
        - 分割线

        ## 代码示例

        ```kotlin
        fun main() {
            println("Hello, Kuikly!")
        }
        ```

        ## 使用方式

        1. 引入 MarkdownRenderer 组件
        2. 传入 Markdown 字符串
        3. 自动解析并渲染

        > 提示：LaTeX 公式和表格将在后续版本通过 Module 桥接实现。

        ---

        以上就是全部功能演示。
    """.trimIndent()

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "Markdown 演示",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        padding(16f)
                    }
                    MarkdownRenderer(markdown = page.demoMarkdown)
                }
            }
        }
    }
}
