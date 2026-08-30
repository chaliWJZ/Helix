package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
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
import com.helix.kuikly.components.ui.OptionSheet
import com.helix.kuikly.components.ui.SheetOption
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingSearchPage]"

internal data class SearchSvcItem(
    val id: String,
    val name: String,
    val type: String,
    val hasApiKey: Boolean,
    val selected: Boolean
)

/**
 * 搜索服务页（对应 Compose 的 SettingSearchPage，真实数据）
 *
 * - pageDidAppear 刷新 getSearchServices
 * - 列表：名称 + 类型 + API Key 状态，当前选中高亮
 * - 点击切换选中（setSearchSelected 持久化，与 Compose 同一 Settings）
 */
@Page("SettingSearchPage")
internal class SettingSearchPage : HelixBasePage() {

    private val services by observableList<SearchSvcItem>()
    private var addSheetVisible by observable(false)
    private var actionSheetVisible by observable(false)
    private var actionTargetId by observable("")
    private var actionTargetName by observable("")
    private var keyEditVisible by observable(false)

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新搜索服务")
        load()
    }

    private fun load() {
        bridge().getSearchServices { json ->
            safeRun("$TAG getSearchServices") {
                val j = json ?: return@safeRun
                val selected = j.optInt("selected")
                val arr = j.optJSONArray("services")
                val items = mutableListOf<SearchSvcItem>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(
                                SearchSvcItem(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    type = o.optString("type"),
                                    hasApiKey = o.optBoolean("hasApiKey"),
                                    selected = i == selected
                                )
                            )
                        }
                    }
                }
                services.clear()
                services.addAll(items)
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 个搜索服务, 选中=$selected")
            }
        }
    }

    /** 点击切换选中（按索引） */
    private fun select(item: SearchSvcItem) {
        val index = services.indexOfFirst { it.id == item.id }
        if (index < 0) return
        KLog.i(UI_LOG, "👆 [Click] $TAG 选中 [${item.name}] index=$index")
        bridge().setSearchSelected(index) { json ->
            safeRun("$TAG setSearchSelected") {
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
                NavBar(
                    title = "搜索服务",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("添加") {
                        KLog.i(UI_LOG, "👆 [Click] $TAG 添加搜索服务")
                        ctx.addSheetVisible = true
                    })
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    vif({ ctx.services.isEmpty() }) {
                        EmptyView(message = "暂无搜索服务", icon = "🔍")
                    }
                    vfor({ ctx.services }) { item ->
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
                                    if (item.selected) HelixTheme.current.primaryContainer
                                    else HelixTheme.current.surfaceContainerLow
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG 服务项[${item.name}]") {
                                        KLog.i(UI_LOG, "👆 [Click] $TAG 服务项 [${item.name}]")
                                        ctx.actionTargetId = item.id
                                        ctx.actionTargetName = item.name
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
                                            if (item.selected) HelixTheme.current.onPrimaryContainer
                                            else HelixTheme.current.onSurface
                                        )
                                    }
                                }
                                Text {
                                    attr {
                                        text(
                                            "${item.type} · " +
                                                (if (item.hasApiKey) "API Key 已配置" else "无需/未配置 Key")
                                        )
                                        fontSize(11f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            if (item.selected) {
                                Text {
                                    attr {
                                        text("✓ 当前使用")
                                        fontSize(11f)
                                        color(HelixTheme.current.primary)
                                        marginLeft(8f)
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== 添加弹层 =====
                OptionSheet(
                    visible = { ctx.addSheetVisible },
                    title = "添加搜索服务",
                    options = listOf(
                        SheetOption("zhipu", "智谱搜索", "需要 API Key"),
                        SheetOption("tavily", "Tavily", "需要 API Key"),
                        SheetOption("exa", "Exa", "需要 API Key"),
                        SheetOption("doubao", "豆包搜索", "需要 API Key"),
                        SheetOption("bing_local", "Bing 本地", "无需 Key"),
                        SheetOption("searxng", "SearXNG", "自建实例")
                    ),
                    currentValue = "",
                    onSelect = { o ->
                        ctx.addSheetVisible = false
                        KLog.i(UI_LOG, "👆 [Click] $TAG 添加搜索 ${o.label}")
                        ctx.bridge().addSearchService(o.value) { json ->
                            safeRun("$TAG addSearchService") {
                                if (json?.optBoolean("ok") == true) {
                                    ctx.bridge().toast("已添加 ${o.label}，点击列表项配置")
                                    ctx.load()
                                }
                            }
                        }
                    },
                    onDismiss = { ctx.addSheetVisible = false }
                )

                // ===== 项操作弹层 =====
                OptionSheet(
                    visible = { ctx.actionSheetVisible },
                    title = "「${ctx.actionTargetName}」操作",
                    options = listOf(
                        SheetOption("select", "设为当前使用"),
                        SheetOption("key", "编辑 API Key"),
                        SheetOption("delete", "删除", "不可恢复")
                    ),
                    currentValue = "",
                    onSelect = { o ->
                        ctx.actionSheetVisible = false
                        when (o.value) {
                            "select" -> {
                                ctx.services.firstOrNull { it.id == ctx.actionTargetId }?.let { ctx.select(it) }
                            }
                            "key" -> ctx.keyEditVisible = true
                            "delete" -> {
                                ctx.bridge().deleteSearchService(ctx.actionTargetId) { json ->
                                    safeRun("$TAG deleteSearchService") {
                                        if (json?.optBoolean("ok") == true) ctx.load()
                                    }
                                }
                            }
                        }
                    },
                    onDismiss = { ctx.actionSheetVisible = false }
                )

                // ===== API Key 编辑弹层 =====
                EditSheet(
                    visible = { ctx.keyEditVisible },
                    title = "编辑 API Key（${ctx.actionTargetName}）",
                    label = "API Key",
                    initialValue = "",
                    onSave = { v: String ->
                        ctx.keyEditVisible = false
                        ctx.bridge().updateSearchApiKey(ctx.actionTargetId, v) { json ->
                            safeRun("$TAG updateSearchApiKey") {
                                if (json?.optBoolean("ok") == true) {
                                    ctx.bridge().toast("已保存")
                                    ctx.load()
                                } else ctx.bridge().toast("该服务无需 Key")
                            }
                        }
                    },
                    onDismiss = { ctx.keyEditVisible = false }
                )
            }
        }
    }
}
