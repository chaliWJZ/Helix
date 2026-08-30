package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.ModelListSheet
import com.helix.kuikly.components.ui.ModelOption
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingModelPage]"

/** 槽位当前值 */
internal data class SlotValue(val id: String, val name: String)

/** 槽位定义（key 与原生桥一致） */
internal data class SlotDef(val key: String, val title: String, val icon: String, val desc: String, val clearable: Boolean = false)

private val SLOTS = listOf(
    SlotDef("chat", "聊天模型", "💬", "处理日常对话的主模型"),
    SlotDef("fast", "快速模型", "⚡", "轻量快速任务（标题/建议等）"),
    SlotDef("title", "标题模型", "🏷️", "生成对话标题，可留空跟随快速模型", clearable = true),
    SlotDef("suggestion", "建议模型", "💡", "生成回复建议，可留空", clearable = true),
    SlotDef("translate", "翻译模型", "🌐", "翻译功能使用"),
    SlotDef("ocr", "OCR 模型", "👁️", "图片文字识别"),
    SlotDef("compress", "压缩模型", "🗜️", "长对话上下文压缩")
)

/**
 * 模型设置页面（对应 Compose 的 SettingModelPage，真实数据）
 *
 * 双 Tab：模型选择（7 槽位真实绑定 SettingsStore）/ 提示词设置
 * - 槽位显示当前模型名，点击弹 ModelListSheet 选择并持久化
 * - 建议模型带「启用建议」开关（enableSuggestion）
 */
@Page("SettingModelPage")
internal class SettingModelPage : HelixBasePage() {

    // 当前 Tab：0=模型 1=提示词
    private var currentTab by observable(0)

    // ===== 真实数据状态 =====
    private val slotValues = mutableMapOf<String, SlotValue?>()
    private var chatModelName by observable("未设置")
    private var fastModelName by observable("未设置")
    private var titleModelName by observable("未设置")
    private var suggestionModelName by observable("未设置")
    private var translateModelName by observable("未设置")
    private var ocrModelName by observable("未设置")
    private var compressModelName by observable("未设置")
    private var enableSuggestion by observable(true)
    private val modelOptions by lazy { mutableListOf<ModelOption>() }

    // 弹层状态
    private var sheetVisible by observable(false)
    private var sheetSlotKey by observable("")
    private var sheetSlotTitle by observable("")
    private var sheetCurrentId by observable("")

    override fun created() {
        super.created()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建，拉取模型设置摘要")
        loadSummary()
    }

    /** 拉取槽位摘要 + 可选模型列表 */
    private fun loadSummary() {
        bridge().getSettingsSummary { json ->
            safeRun("$TAG getSettingsSummary") {
                val j = json ?: run {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG getSettingsSummary 回调为空")
                    return@safeRun
                }
                val slots = j.optJSONObject("slots")
                fun readSlot(key: String): SlotValue? {
                    val o = slots?.optJSONObject(key) ?: return null
                    val id = o.optString("id")
                    return if (id.isEmpty()) null else SlotValue(id, o.optString("name"))
                }
                synchronized(slotValues) {
                    SLOTS.forEach { slotValues[it.key] = readSlot(it.key) }
                }
                fun nameOf(key: String): String = slotValues[key]?.name ?: "未设置"
                chatModelName = nameOf("chat")
                fastModelName = nameOf("fast")
                titleModelName = nameOf("title")
                suggestionModelName = nameOf("suggestion")
                translateModelName = nameOf("translate")
                ocrModelName = nameOf("ocr")
                compressModelName = nameOf("compress")
                enableSuggestion = j.optBoolean("enableSuggestion", true)
                modelOptions.clear()
                j.optJSONArray("models")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            modelOptions.add(
                                ModelOption(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    provider = o.optString("provider")
                                )
                            )
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG 摘要加载完成, 可选模型=${modelOptions.size}, 建议=$enableSuggestion")
            }
        }
    }

    /** 打开某槽位的模型选择弹层 */
    private fun openSlot(def: SlotDef) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 打开槽位 [${def.title}]")
        synchronized(slotValues) {
            sheetCurrentId = slotValues[def.key]?.id ?: ""
        }
        sheetSlotKey = def.key
        sheetSlotTitle = "选择${def.title}"
        sheetVisible = true
    }

    /** 弹层选择模型 → 持久化 */
    private fun selectModel(option: ModelOption) {
        val key = sheetSlotKey
        KLog.i(UI_LOG, "👆 [Click] $TAG 槽位[$key] 选择 ${option.name}")
        bridge().setModelSlot(key, option.id) { json ->
            safeRun("$TAG setModelSlot") {
                val j = json ?: return@safeRun
                val ok = j.optBoolean("ok")
                if (ok) {
                    // 本地立即更新 + 关闭弹层
                    synchronized(slotValues) {
                        slotValues[key] = SlotValue(option.id, "${option.name}（${option.provider}）")
                    }
                    when (key) {
                        "chat" -> chatModelName = "${option.name}（${option.provider}）"
                        "fast" -> fastModelName = "${option.name}（${option.provider}）"
                        "title" -> titleModelName = "${option.name}（${option.provider}）"
                        "suggestion" -> suggestionModelName = "${option.name}（${option.provider}）"
                        "translate" -> translateModelName = "${option.name}（${option.provider}）"
                        "ocr" -> ocrModelName = "${option.name}（${option.provider}）"
                        "compress" -> compressModelName = "${option.name}（${option.provider}）"
                    }
                    sheetVisible = false
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 槽位[$key] 已持久化 → ${option.name}")
                } else {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG setModelSlot 失败: ${j.optString("error")}")
                }
            }
        }
    }

    /** 切换启用建议 */
    private fun toggleSuggestion(enabled: Boolean) {
        bridge().setEnableSuggestion(enabled) { json ->
            safeRun("$TAG setEnableSuggestion") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    enableSuggestion = enabled
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 启用建议 → $enabled")
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
                    title = "模型设置",
                    showBack = true,
                    onBack = { ctx.closePage() }
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
                                safeRun("$TAG 模型Tab") {
                                    KLog.i(UI_LOG, "👆 [Click] $TAG 模型Tab")
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
                                safeRun("$TAG 提示词Tab") {
                                    KLog.i(UI_LOG, "👆 [Click] $TAG 提示词Tab")
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
                    // Tab 0: 模型选择列表（真实数据）
                    vif({ ctx.currentTab == 0 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "聊天模型", subtitle = ctx.chatModelName, icon = "💬", onClick = { ctx.openSlot(SLOTS[0]) }),
                                CardGroupItem(title = "快速模型", subtitle = ctx.fastModelName, icon = "⚡", onClick = { ctx.openSlot(SLOTS[1]) }),
                                CardGroupItem(title = "标题模型", subtitle = ctx.titleModelName, icon = "🏷️", onClick = { ctx.openSlot(SLOTS[2]) })
                            )
                        )
                        // 建议模型：启用开关 + 条件显示模型行
                        View {
                            attr {
                                marginLeft(16f)
                                marginRight(16f)
                                marginTop(8f)
                                marginBottom(8f)
                                borderRadius(12f)
                                backgroundColor(HelixTheme.current.surfaceContainerLow)
                            }
                            SwitchRow(
                                title = "启用建议",
                                subtitle = "在对话后生成回复建议",
                                checked = { ctx.enableSuggestion },
                                onCheckedChange = { ctx.toggleSuggestion(it) }
                            )
                        }
                        if (ctx.enableSuggestion) {
                            CardGroup(
                                items = listOf(
                                    CardGroupItem(title = "建议模型", subtitle = ctx.suggestionModelName, icon = "💡", onClick = { ctx.openSlot(SLOTS[3]) })
                                )
                            )
                        }
                        // 翻译/OCR/压缩
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "翻译模型", subtitle = ctx.translateModelName, icon = "🌐", onClick = { ctx.openSlot(SLOTS[4]) }),
                                CardGroupItem(title = "OCR 模型", subtitle = ctx.ocrModelName, icon = "👁️", onClick = { ctx.openSlot(SLOTS[5]) }),
                                CardGroupItem(title = "压缩模型", subtitle = ctx.compressModelName, icon = "🗜️", onClick = { ctx.openSlot(SLOTS[6]) })
                            )
                        )
                    }
                    // Tab 1: 提示词设置
                    vif({ ctx.currentTab == 1 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "翻译提示词", subtitle = "翻译功能使用的提示词", icon = "📝", onClick = { ctx.openPage("SettingModelPromptPage") }),
                                CardGroupItem(title = "标题提示词", subtitle = "生成对话标题的提示词", icon = "📝", onClick = { ctx.openPage("SettingModelPromptPage") }),
                                CardGroupItem(title = "建议提示词", subtitle = "生成回复建议的提示词", icon = "📝", onClick = { ctx.openPage("SettingModelPromptPage") }),
                                CardGroupItem(title = "OCR 提示词", subtitle = "图片文字识别的提示词", icon = "📝", onClick = { ctx.openPage("SettingModelPromptPage") }),
                                CardGroupItem(title = "上下文压缩提示词", subtitle = "压缩上下文的提示词", icon = "📝", onClick = { ctx.openPage("SettingModelPromptPage") })
                            )
                        )
                    }
                }

                // ===== 模型选择弹层（用 vif 条件渲染，原生 if 不会响应 observable） =====
                vif({ ctx.sheetVisible }) {
                    ModelListSheet(
                        visible = true,
                        title = ctx.sheetSlotTitle,
                        models = ctx.modelOptions.toList(),
                        currentId = ctx.sheetCurrentId,
                        onSelect = { ctx.selectModel(it) },
                        onDismiss = {
                            safeRun("$TAG 关闭弹层") {
                                KLog.i(UI_LOG, "👆 [Click] $TAG 关闭弹层")
                                ctx.sheetVisible = false
                            }
                        }
                    )
                }
            }
        }
    }
}
