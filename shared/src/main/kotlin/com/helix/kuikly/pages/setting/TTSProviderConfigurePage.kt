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
 * TTS 供应商配置页面（对应 Compose 的 TTSProviderConfigure，原1203行）
 *
 * TTS 供应商详细配置：API/音色/语速/试听
 */
@Page("TTSProviderConfigurePage")
internal class TTSProviderConfigurePage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "TTS 供应商配置",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "API Base URL",
                                subtitle = "https://api.openai.com/v1",
                                icon = "🔗",
                                onClick = { ctx.bridge().toast("编辑（待接入）") }
                            ),
                            CardGroupItem(
                                title = "API 密钥",
                                subtitle = "已配置",
                                icon = "🔑",
                                onClick = { ctx.bridge().toast("编辑（待接入）") }
                            ),
                            CardGroupItem(
                                title = "音色",
                                subtitle = "alloy（女声·自然）",
                                icon = "🎵",
                                onClick = { ctx.bridge().toast("选择音色（待接入）") }
                            ),
                            CardGroupItem(
                                title = "语速",
                                subtitle = "1.0x",
                                icon = "⏩",
                                onClick = { ctx.bridge().toast("调节语速（待接入）") }
                            ),
                            CardGroupItem(
                                title = "试听",
                                subtitle = "播放示例语音",
                                icon = "▶️",
                                onClick = { ctx.bridge().toast("正在播放...（待接入）") }
                            )
                        )
                    )
                }
            }
        }
    }
}
