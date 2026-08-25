package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * TTS 语音设置页面（对应 Compose 的 SettingSpeechPage，原964行）
 *
 * 语音引擎管理：系统 TTS + 第三方 TTS 供应商列表
 */
@Page("SettingSpeechPage")
internal class SettingSpeechPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "TTS 服务",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "＋") { ctx.openPage("TTSProviderConfigurePage") }
                    )
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "系统 TTS",
                                subtitle = "使用 Android 系统 TextToSpeech 引擎",
                                icon = "🔊",
                                onClick = { ctx.bridge().toast("系统 TTS 配置（待接入）") }
                            ),
                            CardGroupItem(
                                title = "OpenAI TTS",
                                subtitle = "OpenAI 文字转语音 API",
                                icon = "🎤",
                                onClick = { ctx.openPage("TTSProviderConfigurePage") }
                            ),
                            CardGroupItem(
                                title = "Azure TTS",
                                subtitle = "微软 Azure 语音服务",
                                icon = "🌐",
                                onClick = { ctx.openPage("TTSProviderConfigurePage") }
                            ),
                            CardGroupItem(
                                title = "火山引擎 TTS",
                                subtitle = "火山引擎语音合成",
                                icon = "🌋",
                                onClick = { ctx.openPage("TTSProviderConfigurePage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
