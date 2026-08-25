package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
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
 * 提示词设置页面（对应 Compose 的 PromptSettingsPage / SettingModelPromptPage）
 *
 * 5 个提示词编辑项：翻译/标题/建议/OCR/上下文压缩
 */
@Page("SettingModelPromptPage")
internal class SettingModelPromptPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 编辑器状态
    private var editingIndex by observable(-1)

    private val promptItems = listOf(
        "翻译提示词" to "翻译功能使用的提示词，支持 {text} 和 {lang} 变量",
        "标题提示词" to "生成对话标题的提示词，支持 {content} 变量",
        "建议提示词" to "生成回复建议的提示词，支持 {content} 变量",
        "OCR 提示词" to "图片识别文字的提示词",
        "上下文压缩提示词" to "压缩上下文的提示词"
    )

    override fun body(): ViewBuilder {
        val ctx = this
        val items = promptItems
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "提示词",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = items.map { (title, desc) ->
                            CardGroupItem(
                                title = title,
                                subtitle = desc,
                                icon = "📝",
                                onClick = { ctx.editingIndex = 0 }
                            )
                        }
                    )
                }
            }
        }
    }
}
