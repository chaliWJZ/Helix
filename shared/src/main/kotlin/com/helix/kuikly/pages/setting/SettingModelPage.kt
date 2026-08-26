package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 模型设置页面（对应 Compose 的 SettingModelPage）
 *
 * 双 Tab：模型选择（6种模型槽位）/ 提示词设置
 */
@Page("SettingModelPage")
internal class SettingModelPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 当前 Tab：0=模型 1=提示词
    private var currentTab by observable(0)

    // 模型选择状态（暂用字符串占位，后续接入 Settings Module）
    private var chatModel by observable("未设置")
    private var fastModel by observable("未设置")
    private var titleModel by observable("未设置")
    private var translateModel by observable("未设置")
    private var ocrModel by observable("未设置")
    private var suggestionModel by observable("未设置")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "模型设置",
                    showBack = true,
                    onBack = { ctx.router().closePage() }
                )
                // Tab 切换栏
                View {
                    attr {
                        flexDirectionRow()
                        marginLeft(16f)
                        marginRight(16f)
                        marginTop(8f)
                        marginBottom(8f)
                        borderRadius(12f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        padding(4f)
                    }
                    // Tab: 模型
                    View {
                        attr {
                            flex(1f)
                            height(36f)
                            borderRadius(10f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(if (ctx.currentTab == 0) HelixTheme.current.primary else HelixTheme.current.surfaceContainerLow)
                        }
                        event {
                            click {
                                safeRun("[SettingModelPage#body] 默认模型页-模型Tab") {
                                    KLog.i("HelixKuikly", "👆 [Click] [SettingModelPage#body] 默认模型页-模型Tab")
                                    ctx.currentTab = 0
                            
                                }
                            
                            }
                        }
                        Text {
                            attr {
                                text("模型")
                                fontSize(14f)
                                color(if (ctx.currentTab == 0) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                    // Tab: 提示词
                    View {
                        attr {
                            flex(1f)
                            height(36f)
                            borderRadius(10f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(if (ctx.currentTab == 1) HelixTheme.current.primary else HelixTheme.current.surfaceContainerLow)
                        }
                        event {
                            click {
                                safeRun("[SettingModelPage#body] 默认模型页-提示词Tab") {
                                    KLog.i("HelixKuikly", "👆 [Click] [SettingModelPage#body] 默认模型页-提示词Tab")
                                    ctx.currentTab = 1
                            
                                }
                            
                            }
                        }
                        Text {
                            attr {
                                text("提示词")
                                fontSize(14f)
                                color(if (ctx.currentTab == 1) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                }
                // 内容区域
                View {
                    attr {
                        flex(1f)
                    }
                    // Tab 0: 模型选择列表
                    vif({ ctx.currentTab == 0 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(
                                    title = "聊天模型",
                                    subtitle = ctx.chatModel,
                                    icon = "💬",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                ),
                                CardGroupItem(
                                    title = "快速模型",
                                    subtitle = ctx.fastModel,
                                    icon = "⚡",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                ),
                                CardGroupItem(
                                    title = "标题模型",
                                    subtitle = ctx.titleModel,
                                    icon = "🏷️",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                ),
                                CardGroupItem(
                                    title = "建议模型",
                                    subtitle = ctx.suggestionModel,
                                    icon = "💡",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                ),
                                CardGroupItem(
                                    title = "翻译模型",
                                    subtitle = ctx.translateModel,
                                    icon = "🌐",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                ),
                                CardGroupItem(
                                    title = "OCR 模型",
                                    subtitle = ctx.ocrModel,
                                    icon = "👁️",
                                    onClick = { ctx.router().openPage("ModelPickerPage") }
                                )
                            )
                        )
                    }
                    // Tab 1: 提示词设置
                    vif({ ctx.currentTab == 1 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(
                                    title = "翻译提示词",
                                    subtitle = "翻译功能使用的提示词",
                                    icon = "📝",
                                    onClick = { ctx.router().openPage("SettingModelPromptPage") }
                                ),
                                CardGroupItem(
                                    title = "标题提示词",
                                    subtitle = "生成对话标题的提示词",
                                    icon = "📝",
                                    onClick = { ctx.router().openPage("SettingModelPromptPage") }
                                ),
                                CardGroupItem(
                                    title = "建议提示词",
                                    subtitle = "生成回复建议的提示词",
                                    icon = "📝",
                                    onClick = { ctx.router().openPage("SettingModelPromptPage") }
                                ),
                                CardGroupItem(
                                    title = "OCR 提示词",
                                    subtitle = "图片文字识别的提示词",
                                    icon = "📝",
                                    onClick = { ctx.router().openPage("SettingModelPromptPage") }
                                ),
                                CardGroupItem(
                                    title = "上下文压缩提示词",
                                    subtitle = "压缩上下文的提示词",
                                    icon = "📝",
                                    onClick = { ctx.router().openPage("SettingModelPromptPage") }
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
