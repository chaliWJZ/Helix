package com.helix.kuikly.pages.assistant

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.EditSheet
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[AssistantDetailPage]"

/**
 * 助手详情页（真实数据版，入口：AssistantPage 点击列表项传 assistantId）
 *
 * 3 Tab（Compose 版 7 Tab 的核心子集）：
 * - 基础：名称/温度/TopP/上下文数/MaxTokens/流式输出/网络搜索（编辑+开关）
 * - 提示词：系统提示词编辑
 * - 记忆：记忆/全局记忆/最近聊天引用开关
 * （扩展/请求参数/MCP/本地工具等高级 Tab 在 Compose 版编辑）
 */
@Page("AssistantDetailPage")
internal class AssistantDetailPage : HelixBasePage() {

    private var assistantId by observable("")
    private var name by observable("")
    private var systemPrompt by observable("")
    private var temperature by observable(-1f)
    private var topP by observable(-1f)
    private var contextMessageLimit by observable(0)
    private var streamOutput by observable(true)
    private var enableMemory by observable(false)
    private var useGlobalMemory by observable(false)
    private var enableRecentChatsReference by observable(false)
    private var maxTokens by observable(-1)
    private var enableWebSearch by observable(false)

    private var currentTab by observable(0)
    private var editVisible by observable(false)
    private var editField by observable("")

    override fun created() {
        super.created()
        assistantId = runCatching { pageData.params.optString("assistantId") }.getOrDefault("")
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建 assistantId=$assistantId")
        if (assistantId.isNotEmpty()) load()
    }

    private fun load() {
        bridge().getAssistantDetail(assistantId) { json ->
            safeRun("$TAG getAssistantDetail") {
                val j = json ?: return@safeRun
                if (j.optString("error").isNotEmpty()) {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 拉取失败: ${j.optString("error")}")
                    return@safeRun
                }
                name = j.optString("name")
                systemPrompt = j.optString("systemPrompt")
                temperature = j.optDouble("temperature", -1.0).toFloat()
                topP = j.optDouble("topP", -1.0).toFloat()
                contextMessageLimit = j.optInt("contextMessageLimit")
                streamOutput = j.optBoolean("streamOutput")
                enableMemory = j.optBoolean("enableMemory")
                useGlobalMemory = j.optBoolean("useGlobalMemory")
                enableRecentChatsReference = j.optBoolean("enableRecentChatsReference")
                maxTokens = j.optInt("maxTokens", -1)
                enableWebSearch = j.optBoolean("enableWebSearch")
                KLog.i(UI_LOG, "📦 [Data] $TAG [$name] temp=$temperature memory=$enableMemory")
            }
        }
    }

    private fun saveField(field: String, value: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存[$field] → ${value.take(24)}")
        val params = com.tencent.kuikly.core.nvi.serialization.json.JSONObject().apply {
            put("id", assistantId)
            when (field) {
                "name" -> put("name", value)
                "systemPrompt" -> put("systemPrompt", value)
                "temperature" -> put("temperature", value.toDoubleOrNull() ?: -1.0)
                "topP" -> put("topP", value.toDoubleOrNull() ?: -1.0)
                "contextMessageLimit" -> put("contextMessageLimit", value.toIntOrNull() ?: 0)
                "maxTokens" -> put("maxTokens", value.toIntOrNull() ?: -1)
            }
        }
        bridge().updateAssistantRaw(params) { json ->
            safeRun("$TAG updateAssistant[$field]") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "name" -> name = value
                        "systemPrompt" -> systemPrompt = value
                        "temperature" -> temperature = value.toFloatOrNull() ?: -1f
                        "topP" -> topP = value.toFloatOrNull() ?: -1f
                        "contextMessageLimit" -> contextMessageLimit = value.toIntOrNull() ?: 0
                        "maxTokens" -> maxTokens = value.toIntOrNull() ?: -1
                    }
                    bridge().toast("保存成功")
                } else bridge().toast("保存失败")
            }
        }
    }

    private fun saveBool(field: String, value: Boolean) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 开关[$field] → $value")
        val params = com.tencent.kuikly.core.nvi.serialization.json.JSONObject().apply {
            put("id", assistantId)
            put(field, value)
        }
        bridge().updateAssistantRaw(params) { json ->
            safeRun("$TAG updateAssistant[$field]") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "streamOutput" -> streamOutput = value
                        "enableMemory" -> enableMemory = value
                        "useGlobalMemory" -> useGlobalMemory = value
                        "enableRecentChatsReference" -> enableRecentChatsReference = value
                        "enableWebSearch" -> enableWebSearch = value
                    }
                }
            }
        }
    }

    private fun editArgs(): Triple<String, String, String> = when (editField) {
        "name" -> Triple("编辑助手名称", "名称", name)
        "systemPrompt" -> Triple("编辑系统提示词", "System Prompt（${systemPrompt.length} 字）", systemPrompt)
        "temperature" -> Triple("编辑温度", "Temperature（-1=默认，0-2）", if (temperature < 0) "" else temperature.toString())
        "topP" -> Triple("编辑 TopP", "Top P（-1=默认，0-1）", if (topP < 0) "" else topP.toString())
        "contextMessageLimit" -> Triple("编辑上下文条数上限", "0=不限制", contextMessageLimit.toString())
        "maxTokens" -> Triple("编辑 Max Tokens", "-1=默认", if (maxTokens < 0) "" else maxTokens.toString())
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
                    title = if (ctx.name.isNotEmpty()) ctx.name else "助手详情",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                // 3 Tab
                View {
                    attr {
                        flexDirectionRow()
                        marginLeft(16f); marginRight(16f)
                        marginTop(8f); marginBottom(8f)
                        borderRadius(12f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        padding(4f)
                    }
                    TabBtn("基础", ctx.currentTab == 0) { ctx.currentTab = 0 }
                    TabBtn("提示词", ctx.currentTab == 1) { ctx.currentTab = 1 }
                    TabBtn("记忆", ctx.currentTab == 2) { ctx.currentTab = 2 }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ===== 基础 Tab =====
                    vif({ ctx.currentTab == 0 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "名称", subtitle = ctx.name.ifEmpty { "未命名" }, icon = "🏷️", onClick = {
                                    ctx.editField = "name"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "温度", subtitle = if (ctx.temperature < 0) "默认" else ctx.temperature.toString(), icon = "🌡️", onClick = {
                                    ctx.editField = "temperature"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Top P", subtitle = if (ctx.topP < 0) "默认" else ctx.topP.toString(), icon = "🎚️", onClick = {
                                    ctx.editField = "topP"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "上下文条数上限", subtitle = if (ctx.contextMessageLimit == 0) "不限制" else ctx.contextMessageLimit.toString(), icon = "📏", onClick = {
                                    ctx.editField = "contextMessageLimit"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Max Tokens", subtitle = if (ctx.maxTokens < 0) "默认" else ctx.maxTokens.toString(), icon = "🔢", onClick = {
                                    ctx.editField = "maxTokens"; ctx.editVisible = true
                                })
                            )
                        )
                        View {
                            attr {
                                marginLeft(16f); marginRight(16f); marginBottom(32f)
                                borderRadius(12f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            SwitchRow("流式输出", checked = { ctx.streamOutput }, onCheckedChange = { ctx.saveBool("streamOutput", it) })
                            SwitchRow("网络搜索", subtitle = "该助手独立的搜索开关", checked = { ctx.enableWebSearch }, onCheckedChange = { ctx.saveBool("enableWebSearch", it) })
                        }
                    }

                    // ===== 提示词 Tab =====
                    vif({ ctx.currentTab == 1 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(
                                    title = "系统提示词",
                                    subtitle = if (ctx.systemPrompt.isEmpty()) "未设置（点击编辑）" else ctx.systemPrompt.take(60) + if (ctx.systemPrompt.length > 60) "..." else "",
                                    icon = "📝",
                                    onClick = {
                                        ctx.editField = "systemPrompt"; ctx.editVisible = true
                                    }
                                )
                            )
                        )
                        Text {
                            attr {
                                text("消息模板/预设消息/正则规则等在 Compose 版编辑")
                                fontSize(11f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                                marginBottom(16f)
                                alignSelfCenter()
                            }
                        }
                    }

                    // ===== 记忆 Tab =====
                    vif({ ctx.currentTab == 2 }) {
                        View {
                            attr {
                                marginLeft(16f); marginRight(16f); marginBottom(8f)
                                borderRadius(12f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            SwitchRow("启用记忆", subtitle = "助手自动记忆对话信息", checked = { ctx.enableMemory }, onCheckedChange = { ctx.saveBool("enableMemory", it) })
                            SwitchRow("全局记忆", subtitle = "所有助手共享（关闭=助手隔离）", checked = { ctx.useGlobalMemory }, onCheckedChange = { ctx.saveBool("useGlobalMemory", it) })
                            SwitchRow("最近聊天引用", subtitle = "参考最近聊天内容", checked = { ctx.enableRecentChatsReference }, onCheckedChange = { ctx.saveBool("enableRecentChatsReference", it) })
                        }
                        Text {
                            attr {
                                text("记忆条目管理在 Compose 版编辑")
                                fontSize(11f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(8f)
                                marginBottom(16f)
                                alignSelfCenter()
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
                    onSave = { value: String ->
                        ctx.editVisible = false
                        ctx.saveField(ctx.editField, value)
                    },
                    onDismiss = { ctx.editVisible = false }
                )
            }
        }
    }
}

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
                fontSize(13f)
                fontWeight700()
                color(if (selected) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}
