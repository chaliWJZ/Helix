package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.OptionSheet
import com.helix.kuikly.components.ui.SheetOption
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingSpeechPage]"

internal data class TtsItem(val id: String, val name: String, val type: String, val isDefault: Boolean)
internal data class AsrItem(val id: String, val name: String, val type: String, val isDefault: Boolean)

/**
 * TTS/ASR 服务页（对齐 Compose：底部双Tab TTS/ASR + 完整管理能力）
 *
 * TTS Tab：引擎列表（点击设默认/长按无——点击项弹操作菜单：设为默认/配置/删除）、
 *          添加（选引擎类型）、默认播放速度（选项 0.5x-2.0x）
 * ASR Tab：供应商列表 + 默认切换 + 删除
 */
@Page("SettingSpeechPage")
internal class SettingSpeechPage : HelixBasePage() {

    private val ttsProviders by observableList<TtsItem>()
    private val asrProviders by observableList<AsrItem>()

    private var currentTab by observable(0) // 0=TTS 1=ASR
    private var addTtsSheetVisible by observable(false)
    private var actionSheetVisible by observable(false)
    private var actionTargetId by observable("")
    private var speedSheetVisible by observable(false)
    private var currentSpeed by observable(1.0)

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新 TTS/ASR")
        loadTts()
        loadAsr()
    }

    private fun loadTts() {
        bridge().getTtsProviders { json ->
            safeRun("$TAG getTtsProviders") {
                val j = json ?: return@safeRun
                ttsProviders.clear()
                j.optJSONArray("providers")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            ttsProviders.add(TtsItem(o.optString("id"), o.optString("name"), o.optString("type"), o.optBoolean("isDefault")))
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG TTS ${ttsProviders.size} 个")
            }
        }
    }

    private fun loadAsr() {
        bridge().getASRProviders { json ->
            safeRun("$TAG getASRProviders") {
                val j = json ?: return@safeRun
                asrProviders.clear()
                j.optJSONArray("providers")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            asrProviders.add(AsrItem(o.optString("id"), o.optString("name"), o.optString("type"), o.optBoolean("isDefault")))
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG ASR ${asrProviders.size} 个")
            }
        }
    }

    private fun setDefaultTts(id: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 设默认TTS")
        bridge().setDefaultTts(id) { json ->
            safeRun("$TAG setDefaultTts") {
                if (json?.optBoolean("ok") == true) loadTts()
            }
        }
    }

    private fun setDefaultAsr(id: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 设默认ASR")
        bridge().setDefaultASR(id) { json ->
            safeRun("$TAG setDefaultASR") {
                if (json?.optBoolean("ok") == true) loadAsr()
            }
        }
    }

    private fun deleteTts(id: String) {
        bridge().deleteTtsProvider(id) { json ->
            safeRun("$TAG deleteTtsProvider") {
                if (json?.optBoolean("ok") == true) loadTts()
            }
        }
    }

    private fun deleteAsr(id: String) {
        bridge().deleteAsrProvider(id) { json ->
            safeRun("$TAG deleteAsrProvider") {
                if (json?.optBoolean("ok") == true) loadAsr()
            }
        }
    }

    private fun addTts(type: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 添加TTS type=$type")
        bridge().addTtsProvider(type) { json ->
            safeRun("$TAG addTtsProvider") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    loadTts()
                    bridge().toast("已添加，点击列表项配置")
                }
            }
        }
    }

    private fun openConfigure(id: String) {
        openPage("TTSProviderConfigurePage", JSONObject().apply { put("ttsId", id) })
    }

    private fun setSpeed(speed: Double) {
        bridge().setTtsPlaybackSpeed(speed) { json ->
            safeRun("$TAG setTtsPlaybackSpeed") {
                if (json?.optBoolean("ok") == true) {
                    currentSpeed = speed
                    bridge().toast("已设为 ${speed}x")
                }
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
                NavBar(
                    title = "语音服务",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("添加") { ctx.addTtsSheetVisible = true })
                )
                // 双 Tab 栏
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
                    vif({ ctx.currentTab == 0 }) {
                        TabBtn("TTS", true) { ctx.currentTab = 0 }
                        TabBtn("ASR", false) { ctx.currentTab = 1 }
                    }
                    velse {
                        TabBtn("TTS", false) { ctx.currentTab = 0 }
                        TabBtn("ASR", true) { ctx.currentTab = 1 }
                    }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ==================== TTS Tab ====================
                    vif({ ctx.currentTab == 0 }) {
                        // 默认播放速度
                        View {
                            attr {
                                marginLeft(16f); marginRight(16f); marginBottom(8f)
                                padding(12f)
                                borderRadius(12f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                                flexDirectionRow()
                                alignItemsCenter()
                            }
                            event {
                                click {
                                    safeRun("$TAG 播放速度") {
                                        KLog.i(UI_LOG, "👆 [Click] $TAG 播放速度选择")
                                        ctx.speedSheetVisible = true
                                    }
                                }
                            }
                            View {
                                attr { flex(1f) }
                                Text {
                                    attr {
                                        text("默认播放速度")
                                        fontSize(14f)
                                        color(HelixTheme.current.onSurface)
                                    }
                                }
                            }
                            Text {
                                attr {
                                    text("${ctx.currentSpeed}x ›")
                                    fontSize(13f)
                                    color(HelixTheme.current.primary)
                                }
                            }
                        }
                        // TTS 列表
                        vfor({ ctx.ttsProviders }) { item ->
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    marginLeft(16f); marginRight(16f); marginBottom(1f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(
                                        if (item.isDefault) HelixTheme.current.primaryContainer
                                        else HelixTheme.current.surfaceContainerLow
                                    )
                                }
                                event {
                                    click {
                                        safeRun("$TAG TTS项[${item.name}]") {
                                            KLog.i(UI_LOG, "👆 [Click] $TAG TTS项 [${item.name}]")
                                            ctx.actionTargetId = item.id
                                            ctx.actionSheetVisible = true
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
                                        }
                                    }
                                }
                            }
                        }
                        Text {
                            attr {
                                text("点击列表项：设为默认 / 配置 / 删除")
                                fontSize(11f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                                marginBottom(16f)
                                alignSelfCenter()
                            }
                        }
                    }

                    // ==================== ASR Tab ====================
                    velse {
                        vif({ ctx.asrProviders.isEmpty() }) {
                            EmptyView(message = "暂无 ASR 供应商，在 Compose 版添加后同步显示", icon = "🎙️")
                        }
                        vfor({ ctx.asrProviders }) { item ->
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    marginLeft(16f); marginRight(16f); marginBottom(1f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(
                                        if (item.isDefault) HelixTheme.current.primaryContainer
                                        else HelixTheme.current.surfaceContainerLow
                                    )
                                }
                                event {
                                    click {
                                        safeRun("$TAG ASR项[${item.name}]") { ctx.setDefaultAsr(item.id) }
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
                                // 删除
                                Text {
                                    attr {
                                        text("删除"); fontSize(12f); color(HelixTheme.current.error)
                                        marginLeft(8f); marginTop(6f); marginBottom(6f)
                                    }
                                    event {
                                        click {
                                            safeRun("$TAG 删除ASR[${item.name}]") { ctx.deleteAsr(item.id) }
                                        }
                                    }
                                }
                                if (item.isDefault) {
                                    Text {
                                        attr {
                                            text("✓")
                                            fontSize(14f)
                                            color(HelixTheme.current.primary)
                                            marginLeft(8f)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== 添加 TTS 弹层（11 引擎类型） =====
                OptionSheet(
                    visible = { ctx.addTtsSheetVisible },
                    title = "添加 TTS 引擎",
                    options = listOf(
                        SheetOption("openai", "OpenAI TTS"),
                        SheetOption("gemini", "Gemini TTS"),
                        SheetOption("system", "系统 TTS"),
                        SheetOption("minimax", "MiniMax"),
                        SheetOption("qwen", "通义千问"),
                        SheetOption("groq", "Groq"),
                        SheetOption("xai", "xAI"),
                        SheetOption("mimo", "MiMo"),
                        SheetOption("elevenlabs", "ElevenLabs"),
                        SheetOption("step", "Step"),
                        SheetOption("fish-audio", "Fish Audio")
                    ),
                    currentValue = "",
                    onSelect = { o ->
                        ctx.addTtsSheetVisible = false
                        ctx.addTts(o.value)
                    },
                    onDismiss = { ctx.addTtsSheetVisible = false }
                )

                // ===== TTS 项操作弹层 =====
                OptionSheet(
                    visible = { ctx.actionSheetVisible },
                    title = "TTS 引擎操作",
                    options = listOf(
                        SheetOption("default", "设为默认"),
                        SheetOption("configure", "配置（名称/Key/URL）"),
                        SheetOption("delete", "删除", "不可恢复")
                    ),
                    currentValue = "",
                    onSelect = { o ->
                        ctx.actionSheetVisible = false
                        when (o.value) {
                            "default" -> ctx.setDefaultTts(ctx.actionTargetId)
                            "configure" -> ctx.openConfigure(ctx.actionTargetId)
                            "delete" -> ctx.deleteTts(ctx.actionTargetId)
                        }
                    },
                    onDismiss = { ctx.actionSheetVisible = false }
                )

                // ===== 播放速度弹层 =====
                OptionSheet(
                    visible = { ctx.speedSheetVisible },
                    title = "默认播放速度",
                    options = listOf(0.5, 0.75, 1.0, 1.25, 1.5, 2.0).map {
                        SheetOption(it.toString(), "${it}x")
                    },
                    currentValue = ctx.currentSpeed.toString(),
                    onSelect = { o ->
                        ctx.speedSheetVisible = false
                        ctx.setSpeed(o.value.toDouble())
                    },
                    onDismiss = { ctx.speedSheetVisible = false }
                )
            }
        }
    }
}

/** Tab 按钮（顶层扩展避免 implicit receiver 歧义） */
private fun ViewContainer<*, *>.TabBtn(label: String, selected: Boolean, onClick: () -> Unit) {
    View {
        attr {
            height(40f)
            flex(1f)
            borderRadius(10f)
            justifyContentCenter()
            alignItemsCenter()
            backgroundColor(
                if (selected) HelixTheme.current.primary else HelixTheme.current.surfaceContainerLow
            )
        }
        event {
            click {
                safeRun("$TAG Tab[$label]") {
                    KLog.i(UI_LOG, "👆 [Click] $TAG 切到[$label]")
                    onClick()
                }
            }
        }
        Text {
            attr {
                text(label)
                fontSize(14f)
                fontWeight700()
                color(if (selected) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}