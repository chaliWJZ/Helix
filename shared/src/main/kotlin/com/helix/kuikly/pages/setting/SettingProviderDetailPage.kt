package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 供应商详情页面（对应 Compose 的 SettingProviderDetailPage，原1572行）
 *
 * 供应商配置：API密钥/ baseURL / 模型列表 / 连通性测试
 */
@Page("SettingProviderDetailPage")
internal class SettingProviderDetailPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "供应商详情",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    // 基础配置
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "API Base URL",
                                subtitle = "https://api.openai.com/v1",
                                icon = "🔗",
                                onClick = { ctx.bridge().toast("编辑 Base URL（待接入）") }
                            ),
                            CardGroupItem(
                                title = "API 密钥",
                                subtitle = "已配置（sk-****...****）",
                                icon = "🔑",
                                onClick = { ctx.bridge().toast("编辑 API Key（待接入）") }
                            ),
                            CardGroupItem(
                                title = "测试连通性",
                                subtitle = "发送测试请求验证配置",
                                icon = "📡",
                                onClick = { ctx.bridge().toast("正在测试...（待接入）") }
                            )
                        )
                    )
                    // 模型列表
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "gpt-4o",
                                subtitle = "对话 · 128K 上下文",
                                icon = "🤖",
                                onClick = { ctx.bridge().toast("模型详情（待接入）") }
                            ),
                            CardGroupItem(
                                title = "gpt-4o-mini",
                                subtitle = "对话 · 快速响应",
                                icon = "⚡",
                                onClick = { ctx.bridge().toast("模型详情（待接入）") }
                            ),
                            CardGroupItem(
                                title = "dall-e-3",
                                subtitle = "图片生成",
                                icon = "🎨",
                                onClick = { ctx.bridge().toast("模型详情（待接入）") }
                            ),
                            CardGroupItem(
                                title = "text-embedding-3-large",
                                subtitle = "向量嵌入",
                                icon = "📊",
                                onClick = { ctx.bridge().toast("模型详情（待接入）") }
                            )
                        )
                    )
                }
            }
        }
    }
}
