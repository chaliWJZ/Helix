package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.ConfirmSheet
import com.helix.kuikly.components.ui.EditSheet
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[TTSProviderConfigurePage]"

/**
 * TTS 引擎配置页（真实数据，入口：SettingSpeechPage 点列表项→配置）
 *
 * - 按 pageData.params.ttsId 加载真实配置
 * - 名称/API Key/Base URL 编辑（EditSheet 即时保存）
 * - 模型/音色只读展示（音色选择等高级配置在 Compose 版）
 * - 设为默认 / 删除（二次确认）
 */
@Page("TTSProviderConfigurePage")
internal class TTSProviderConfigurePage : HelixBasePage() {

    private var ttsId by observable("")
    private var name by observable("")
    private var type by observable("")
    private var isDefault by observable(false)
    private var apiKey by observable("")
    private var baseUrl by observable("")
    private var model by observable("")
    private var voice by observable("")

    private var editVisible by observable(false)
    private var editField by observable("")
    private var confirmDeleteVisible by observable(false)

    override fun created() {
        super.created()
        ttsId = runCatching { pageData.params.optString("ttsId") }.getOrDefault("")
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建 ttsId=$ttsId")
        if (ttsId.isNotEmpty()) load()
    }

    private fun load() {
        bridge().getTtsDetail(ttsId) { json ->
            safeRun("$TAG getTtsDetail") {
                val j = json ?: return@safeRun
                if (j.optString("error").isNotEmpty()) {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 拉取失败: ${j.optString("error")}")
                    return@safeRun
                }
                name = j.optString("name")
                type = j.optString("type")
                isDefault = j.optBoolean("isDefault")
                apiKey = j.optString("apiKey")
                baseUrl = j.optString("baseUrl")
                model = j.optString("model")
                voice = j.optString("voice")
                KLog.i(UI_LOG, "📦 [Data] $TAG [$name] type=$type model=$model")
            }
        }
    }

    private fun save(field: String, value: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存[$field] → ${value.take(24)}")
        val params = com.tencent.kuikly.core.nvi.serialization.json.JSONObject().apply {
            put("id", ttsId)
            when (field) {
                "name" -> put("name", value)
                "apiKey" -> put("apiKey", value)
                "baseUrl" -> put("baseUrl", value)
            }
        }
        bridge().updateTtsRaw(params) { json ->
            safeRun("$TAG updateTts[$field]") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "name" -> name = value
                        "apiKey" -> apiKey = value
                        "baseUrl" -> baseUrl = value
                    }
                    bridge().toast("保存成功")
                } else bridge().toast("保存失败")
            }
        }
    }

    private fun setDefault() {
        bridge().setDefaultTts(ttsId) { json ->
            safeRun("$TAG setDefaultTts") {
                if (json?.optBoolean("ok") == true) {
                    isDefault = true
                    bridge().toast("已设为默认")
                }
            }
        }
    }

    private fun delete() {
        bridge().deleteTtsProvider(ttsId) { json ->
            safeRun("$TAG deleteTtsProvider") {
                if (json?.optBoolean("ok") == true) closePage()
            }
        }
    }

    private fun editArgs(): Triple<String, String, String> = when (editField) {
        "name" -> Triple("编辑引擎名称", "名称", name)
        "apiKey" -> Triple("编辑 API Key", "API Key（${if (apiKey.isEmpty()) "未配置" else "已配置"}）", "")
        "baseUrl" -> Triple("编辑 Base URL", "Base URL", baseUrl)
        else -> Triple("编辑", "", "")
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
                    title = if (ctx.name.isNotEmpty()) ctx.name else "TTS 配置",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "引擎类型",
                                subtitle = ctx.type,
                                icon = "📢"
                            ),
                            CardGroupItem(
                                title = "名称",
                                subtitle = ctx.name,
                                icon = "🏷️",
                                onClick = { ctx.editField = "name"; ctx.editVisible = true }
                            ),
                            CardGroupItem(
                                title = "API Key",
                                subtitle = if (ctx.apiKey.isEmpty()) "未配置" else "已配置（点击编辑）",
                                icon = "🔑",
                                onClick = { ctx.editField = "apiKey"; ctx.editVisible = true }
                            ),
                            CardGroupItem(
                                title = "Base URL",
                                subtitle = ctx.baseUrl.ifEmpty { "默认" },
                                icon = "🔗",
                                onClick = { ctx.editField = "baseUrl"; ctx.editVisible = true }
                            ),
                            CardGroupItem(
                                title = "模型",
                                subtitle = ctx.model.ifEmpty { "默认" },
                                icon = "🤖"
                            ),
                            CardGroupItem(
                                title = "音色",
                                subtitle = ctx.voice.ifEmpty { "默认音色（高级音色选择在 Compose 版）" },
                                icon = "🎤"
                            )
                        )
                    )

                    // 设为默认
                    vif({ !ctx.isDefault }) {
                        View {
                            attr {
                                height(44f)
                                borderRadius(22f)
                                justifyContentCenter()
                                alignItemsCenter()
                                marginLeft(16f); marginRight(16f)
                                marginTop(8f)
                                backgroundColor(HelixTheme.current.primary)
                            }
                            event {
                                click { safeRun("$TAG 设为默认") { ctx.setDefault() } }
                            }
                            Text {
                                attr {
                                    text("设为默认引擎")
                                    fontSize(15f)
                                    color(HelixTheme.current.onPrimary)
                                }
                            }
                        }
                    }
                    vif({ ctx.isDefault }) {
                        Text {
                            attr {
                                text("✓ 当前默认引擎")
                                fontSize(13f)
                                color(HelixTheme.current.primary)
                                marginTop(12f)
                                marginBottom(4f)
                                alignSelfCenter()
                            }
                        }
                    }

                    // 删除
                    View {
                        attr {
                            height(44f)
                            borderRadius(22f)
                            justifyContentCenter()
                            alignItemsCenter()
                            marginLeft(16f); marginRight(16f)
                            marginTop(12f); marginBottom(32f)
                            backgroundColor(HelixTheme.current.errorContainer)
                        }
                        event {
                            click { safeRun("$TAG 删除") { ctx.confirmDeleteVisible = true } }
                        }
                        Text {
                            attr {
                                text("删除此引擎")
                                fontSize(15f)
                                color(HelixTheme.current.onErrorContainer)
                            }
                        }
                    }
                }

                val (t, l, v) = ctx.editArgs()
                EditSheet(
                    visible = { ctx.editVisible },
                    title = t,
                    label = l,
                    initialValue = v,
                    maskDisplay = ctx.editField == "apiKey",
                    onSave = { value ->
                        ctx.editVisible = false
                        ctx.save(ctx.editField, value)
                    },
                    onDismiss = { ctx.editVisible = false }
                )

                ConfirmSheet(
                    visible = { ctx.confirmDeleteVisible },
                    title = "删除 TTS 引擎",
                    message = "确定删除「${ctx.name}」？不可恢复。",
                    confirmText = "删除",
                    onConfirm = {
                        ctx.confirmDeleteVisible = false
                        ctx.delete()
                    },
                    onDismiss = { ctx.confirmDeleteVisible = false }
                )
            }
        }
    }
}
