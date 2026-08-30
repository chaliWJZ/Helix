package com.helix.kuikly.pages.extensions

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.EditSheet
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[PromptPage]"

internal data class ModeItem(val id: String, val name: String, val enabled: Boolean, val position: String, val contentLength: Int)
internal data class LorebookItem(val id: String, val name: String, val description: String, val enabled: Boolean, val entryCount: Int)

/**
 * 提示词管理页（对齐 Compose：双Tab 模式注入/世界书 + 真实数据 + CRUD）
 *
 * 模式注入Tab：列表（启停/编辑/删除）+ 新建（名称弹层→内容弹层）
 * 世界书Tab：列表（启停/删除，条目编辑在 Compose 版）
 */
@Page("PromptPage")
internal class PromptPage : HelixBasePage() {

    private val modes by observableList<ModeItem>()
    private val lorebooks by observableList<LorebookItem>()

    private var currentTab by observable(0) // 0=模式注入 1=世界书
    // 模式注入编辑流
    private var editNameVisible by observable(false)
    private var editContentVisible by observable(false)
    private var editingId by observable("")
    private var draftName by observable("")
    private var draftContent by observable("")
    private var actionSheetVisible by observable(false)
    private var actionTargetId by observable("")

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新")
        loadModes()
        loadLorebooks()
    }

    private fun loadModes() {
        bridge().getModeInjections { json ->
            safeRun("$TAG getModeInjections") {
                val j = json ?: return@safeRun
                modes.clear()
                j.optJSONArray("modes")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            modes.add(ModeItem(o.optString("id"), o.optString("name"), o.optBoolean("enabled"), o.optString("position"), o.optInt("contentLength")))
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG 模式注入 ${modes.size} 条")
            }
        }
    }

    private fun loadLorebooks() {
        bridge().getLorebooks { json ->
            safeRun("$TAG getLorebooks") {
                val j = json ?: return@safeRun
                lorebooks.clear()
                j.optJSONArray("lorebooks")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            lorebooks.add(LorebookItem(o.optString("id"), o.optString("name"), o.optString("description"), o.optBoolean("enabled"), o.optInt("entryCount")))
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG 世界书 ${lorebooks.size} 本")
            }
        }
    }

    /** 新建流程：名称 → 内容 */
    private fun startCreate() {
        editingId = ""
        draftName = ""
        draftContent = ""
        editNameVisible = true
    }

    private fun saveMode() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存模式注入 [$draftName]")
        bridge().saveModeInjection(editingId, draftName, draftContent) { json ->
            safeRun("$TAG saveModeInjection") {
                if (json?.optBoolean("ok") == true) {
                    bridge().toast("已保存")
                    loadModes()
                }
            }
        }
    }

    private fun toggleMode(item: ModeItem, enabled: Boolean) {
        bridge().setModeInjectionEnabled(item.id, enabled) { json ->
            safeRun("$TAG setModeInjectionEnabled") {
                if (json?.optBoolean("ok") == true) loadModes()
            }
        }
    }

    private fun deleteMode(id: String) {
        bridge().deleteModeInjection(id) { json ->
            safeRun("$TAG deleteModeInjection") {
                if (json?.optBoolean("ok") == true) loadModes()
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
                    title = "提示词",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("新建") {
                        KLog.i(UI_LOG, "👆 [Click] $TAG 新建模式注入")
                        ctx.startCreate()
                    })
                )
                // 双 Tab
                View {
                    attr {
                        flexDirectionRow()
                        marginLeft(16f); marginRight(16f)
                        marginTop(8f); marginBottom(8f)
                        borderRadius(12f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        padding(4f)
                    }
                    vif({ ctx.currentTab == 0 }) {
                        TabBtn("模式注入", true) { ctx.currentTab = 0 }
                        TabBtn("世界书", false) { ctx.currentTab = 1 }
                    }
                    velse {
                        TabBtn("模式注入", false) { ctx.currentTab = 0 }
                        TabBtn("世界书", true) { ctx.currentTab = 1 }
                    }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ===== 模式注入 Tab =====
                    vif({ ctx.currentTab == 0 }) {
                        vif({ ctx.modes.isEmpty() }) {
                            EmptyView(message = "暂无模式注入，点击右上角「新建」", icon = "📝")
                        }
                        vfor({ ctx.modes }) { item ->
                            View {
                                attr {
                                    flexDirectionColumn()
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(
                                        if (item.enabled) HelixTheme.current.surfaceContainerLow
                                        else HelixTheme.current.errorContainer
                                    )
                                }
                                View {
                                    attr { flexDirectionRow(); alignItemsCenter() }
                                    View {
                                        attr { flex(1f) }
                                        Text {
                                            attr {
                                                text(item.name.ifEmpty { "未命名" }
                                                )
                                                fontSize(15f)
                                                fontWeight700()
                                                color(HelixTheme.current.onSurface)
                                            }
                                        }
                                        Text {
                                            attr {
                                                text("${item.position} · ${item.contentLength} 字")
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
                                                safeRun("$TAG 删除模式[${item.name}]") { ctx.deleteMode(item.id) }
                                            }
                                        }
                                    }
                                }
                                SwitchRow(
                                    title = "启用",
                                    checked = { item.enabled },
                                    onCheckedChange = { ctx.toggleMode(item, it) }
                                )
                            }
                        }
                    }

                    // ===== 世界书 Tab =====
                    velse {
                        vif({ ctx.lorebooks.isEmpty() }) {
                            EmptyView(message = "暂无世界书，在 Compose 版创建后同步显示", icon = "📚")
                        }
                        vfor({ ctx.lorebooks }) { item ->
                            View {
                                attr {
                                    flexDirectionColumn()
                                    marginLeft(16f); marginRight(16f); marginBottom(8f)
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(
                                        if (item.enabled) HelixTheme.current.surfaceContainerLow
                                        else HelixTheme.current.errorContainer
                                    )
                                }
                                View {
                                    attr { flexDirectionRow(); alignItemsCenter() }
                                    View {
                                        attr { flex(1f) }
                                        Text {
                                            attr {
                                                text(item.name.ifEmpty { "未命名" })
                                                fontSize(15f)
                                                fontWeight700()
                                                color(HelixTheme.current.onSurface)
                                            }
                                        }
                                        Text {
                                            attr {
                                                text("${item.entryCount} 条规则 · ${item.description}")
                                                fontSize(11f)
                                                color(HelixTheme.current.onSurfaceVariant)
                                                marginTop(2f)
                                            }
                                        }
                                    }
                                    Text {
                                        attr {
                                            text("删除"); fontSize(12f); color(HelixTheme.current.error)
                                            marginLeft(8f); marginTop(6f); marginBottom(6f)
                                        }
                                        event {
                                            click {
                                                safeRun("$TAG 删除世界书[${item.name}]") {
                                                    ctx.bridge().deleteLorebook(item.id) { json ->
                                                        if (json?.optBoolean("ok") == true) ctx.loadLorebooks()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                SwitchRow(
                                    title = "启用",
                                    checked = { item.enabled },
                                    onCheckedChange = { enabled ->
                                        ctx.bridge().setLorebookEnabled(item.id, enabled) { json ->
                                            if (json?.optBoolean("ok") == true) ctx.loadLorebooks()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // ===== 新建/编辑：名称弹层 =====
                EditSheet(
                    visible = { ctx.editNameVisible },
                    title = if (ctx.editingId.isEmpty()) "新建模式注入" else "编辑名称",
                    label = "名称",
                    initialValue = ctx.draftName,
                    onSave = { v: String ->
                        ctx.draftName = v
                        ctx.editNameVisible = false
                        ctx.editContentVisible = true
                    },
                    onDismiss = { ctx.editNameVisible = false }
                )
                // 内容弹层
                EditSheet(
                    visible = { ctx.editContentVisible },
                    title = "注入内容（[${ctx.draftName}]）",
                    label = "提示词内容",
                    initialValue = ctx.draftContent,
                    onSave = { v: String ->
                        ctx.draftContent = v
                        ctx.editContentVisible = false
                        ctx.saveMode()
                    },
                    onDismiss = { ctx.editContentVisible = false }
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
                fontSize(14f)
                fontWeight700()
                color(if (selected) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}
