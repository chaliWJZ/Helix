package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingSpeechPage]"

internal data class TtsItem(
    val id: String,
    val name: String,
    val type: String,
    val isDefault: Boolean
)

/**
 * TTS 服务页（对应 Compose 的 SettingSpeechPage，真实数据）
 *
 * - pageDidAppear 刷新 getTtsProviders
 * - 列表：名称 + 引擎类型，默认引擎高亮（RadioButton 风格）
 * - 点击切换默认引擎（setDefaultTts 持久化）
 */
@Page("SettingSpeechPage")
internal class SettingSpeechPage : HelixBasePage() {

    private val providers by observableList<TtsItem>()

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新 TTS 列表")
        load()
    }

    private fun load() {
        bridge().getTtsProviders { json ->
            safeRun("$TAG getTtsProviders") {
                val j = json ?: return@safeRun
                val arr = j.optJSONArray("providers")
                val items = mutableListOf<TtsItem>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(
                                TtsItem(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    type = o.optString("type"),
                                    isDefault = o.optBoolean("isDefault")
                                )
                            )
                        }
                    }
                }
                providers.clear()
                providers.addAll(items)
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 个 TTS 引擎")
            }
        }
    }

    private fun select(item: TtsItem) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 设为默认 [${item.name}] type=${item.type}")
        bridge().setDefaultTts(item.id) { json ->
            safeRun("$TAG setDefaultTts") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) load()
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(title = "TTS 服务", showBack = true, onBack = { ctx.closePage() })
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    vif({ ctx.providers.isEmpty() }) {
                        EmptyView(message = "暂无 TTS 引擎", icon = "📢")
                    }
                    vfor({ ctx.providers }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(1f)
                                padding(12f)
                                borderRadius(12f)
                                backgroundColor(
                                    if (item.isDefault) HelixTheme.current.primaryContainer
                                    else HelixTheme.current.surfaceContainerLow
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG 引擎项[${item.name}]") { ctx.select(item) }
                                }
                            }
                            // RadioButton 风格圆点
                            View {
                                attr {
                                    width(20f)
                                    height(20f)
                                    borderRadius(10f)
                                    border(Border(2f, BorderStyle.SOLID, HelixTheme.current.primary))
                                    justifyContentCenter()
                                    alignItemsCenter()
                                    marginRight(12f)
                                }
                                if (item.isDefault) {
                                    View {
                                        attr {
                                            width(10f)
                                            height(10f)
                                            borderRadius(5f)
                                            backgroundColor(HelixTheme.current.primary)
                                        }
                                    }
                                }
                            }
                            View {
                                attr { flex(1f) }
                                Text {
                                    attr {
                                        text(item.name)
                                        fontSize(15f)
                                        fontWeight700()
                                        color(
                                            if (item.isDefault) HelixTheme.current.onPrimaryContainer
                                            else HelixTheme.current.onSurface
                                        )
                                    }
                                }
                                Text {
                                    attr {
                                        text(item.type)
                                        fontSize(11f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            if (item.isDefault) {
                                Text {
                                    attr {
                                        text("默认")
                                        fontSize(11f)
                                        color(HelixTheme.current.primary)
                                        marginLeft(8f)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
