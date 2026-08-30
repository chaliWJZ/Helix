package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.DragPreview
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.OptionSheet
import com.helix.kuikly.components.ui.ReorderableItem
import com.helix.kuikly.components.ui.SheetOption
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import kotlin.math.roundToInt

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingProviderPage]"
private const val ROW_HEIGHT = 66f // 列表项行高（含间距，拖拽跨行计算用）

internal data class ProviderItem(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val modelCount: Int,
    val type: String
)

internal data class RecProvider(val id: String, val name: String, val type: String, val baseUrl: String)

/**
 * 供应商列表页（精复刻 Compose SettingProviderPage UI 形态）
 *
 * - TopBar 右上 3 个图标按钮（对齐 Compose actions）：✨推荐 / 📱导入(剪贴板JSON) / ➕添加
 * - 搜索框实时过滤（本地）
 * - 列表项：图标+名称+启停/模型数/类型 + 拖动手柄(⋮⋮ pan 手势排序，非上下移按钮)
 *   拖动 = 触觉反馈 + 浮动预览跟随手指 + 原位半透明占位 + 松手按位移跨行落位（moveProvider 桥）
 * - 点击列表项跳详情（providerId）
 */
@Page("SettingProviderPage")
internal class SettingProviderPage : HelixBasePage() {

    private val providers by observableList<ProviderItem>()
    private val filteredList by observableList<ProviderItem>()
    private val recProviders by observableList<RecProvider>()
    private val addedRecIds by observableList<String>()

    private var searchText by observable("")
    private var addSheetVisible by observable(false)
    private var recSheetVisible by observable(false)
    private var customSheetVisible by observable(false)
    private var typeSheetVisible by observable(false)
    // 拖拽状态机（对齐 Compose ReorderableItem）
    private var dragIndex by observable(-1)          // -1=非拖动
    private var dragStartPageY by observable(0f)
    private var dragPageY by observable(0f)
    private var dragLabel by observable("")
    // 自定义添加表单
    private var customName by observable("")
    private var customType by observable("openai")
    private var customBaseUrl by observable("")
    private var customApiKey by observable("")

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新供应商列表")
        loadProviders()
    }

    private fun loadProviders() {
        bridge().getProviders { json ->
            safeRun("$TAG getProviders") {
                val j = json ?: return@safeRun
                val arr = j.optJSONArray("providers")
                val items = mutableListOf<ProviderItem>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(
                                ProviderItem(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    enabled = o.optBoolean("enabled"),
                                    modelCount = o.optInt("modelCount"),
                                    type = o.optString("type")
                                )
                            )
                        }
                    }
                }
                providers.clear()
                providers.addAll(items)
                addedRecIds.clear()
                addedRecIds.addAll(items.map { it.id })
                applyFilter()
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 个供应商")
            }
        }
    }

    private fun applyFilter() {
        val list = if (searchText.isEmpty()) providers.toList()
        else providers.filter { it.name.contains(searchText, true) || it.type.contains(searchText, true) }
        filteredList.clear()
        filteredList.addAll(list)
    }

    private fun loadRecommended() {
        bridge().getRecommendedProviders { json ->
            safeRun("$TAG getRecommendedProviders") {
                val j = json ?: return@safeRun
                val arr = j.optJSONArray("recommended")
                val items = mutableListOf<RecProvider>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(RecProvider(o.optString("id"), o.optString("name"), o.optString("type"), o.optString("baseUrl")))
                        }
                    }
                }
                recProviders.clear()
                recProviders.addAll(items)
                KLog.i(UI_LOG, "📦 [Data] $TAG 推荐供应商 ${items.size} 个")
            }
        }
    }

    private fun addRecommended(rec: RecProvider) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 一键添加推荐 [${rec.name}]")
        bridge().addRecommendedProvider(rec.id) { json ->
            safeRun("$TAG addRecommendedProvider") {
                if (json?.optBoolean("ok") == true) {
                    bridge().toast("已添加 ${rec.name}")
                    recSheetVisible = false
                    loadProviders()
                } else {
                    bridge().toast(json?.optString("error") ?: "添加失败")
                }
            }
        }
    }

    /** 📱 从剪贴板导入供应商 JSON（对应 Compose ImportProviderButton 的手动导入路径） */
    private fun importFromClipboard() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 从剪贴板导入供应商")
        bridge().importProviderClipboard { json ->
            safeRun("$TAG importProviderClipboard") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    bridge().toast("已导入 ${j.optString("name")}（在 Compose 版复制供应商 JSON 后在此粘贴导入）")
                    loadProviders()
                } else {
                    bridge().toast(j.optString("error"))
                }
            }
        }
    }

    private fun submitCustom() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 自定义添加 [$customName] type=$customType")
        bridge().addCustomProvider(customName, customType, customBaseUrl, customApiKey) { json ->
            safeRun("$TAG addCustomProvider") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    bridge().toast("已添加 ${customName.ifEmpty { customType }}")
                    customSheetVisible = false
                    customName = ""; customBaseUrl = ""; customApiKey = ""
                    loadProviders()
                } else {
                    bridge().toast(j.optString("error").ifEmpty { "添加失败" })
                }
            }
        }
    }

    // ==================== 拖拽状态机（对齐 Compose ReorderableItem） ====================

    private var dragItem by observable<ProviderItem?>(null) // 拖动中项的完整信息（用于预览卡显示）

    private fun onDragStart(index: Int, item: ProviderItem, pageY: Float) {
        dragIndex = index
        dragStartPageY = pageY
        dragPageY = pageY
        dragLabel = item.name
        dragItem = item
        bridge().vibrate(50) // GestureThresholdActivate
        KLog.i(UI_LOG, "👆 [Drag] $TAG 开始拖拽[$index ${item.name}] pageY=$pageY")
    }

    private fun onDragMove(pageY: Float) {
        dragPageY = pageY
    }

    private fun onDragEnd() {
        if (dragIndex < 0) return
        val from = dragIndex
        // 位移换算跨行数（对齐 Compose reorderableState 的 onDrag 回调逻辑）
        val deltaRows = ((dragPageY - dragStartPageY) / ROW_HEIGHT).roundToInt()
        val to = (from + deltaRows).coerceIn(0, providers.size - 1)
        dragIndex = -1
        dragItem = null
        bridge().vibrate(30) // GestureEnd
        if (to != from) {
            KLog.i(UI_LOG, "👆 [Drag] $TAG 落位 $from→$to (Δ$deltaRows 行)")
            bridge().moveProvider(from, to) { json ->
                safeRun("$TAG moveProvider") {
                    if (json?.optBoolean("ok") == true) loadProviders()
                }
            }
        } else {
            KLog.i(UI_LOG, "👆 [Drag] $TAG 原位放下 (Δ$deltaRows 行)")
        }
    }

    private fun openDetail(item: ProviderItem) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 打开供应商 [${item.name}] type=${item.type}")
        openPage(
            "SettingProviderDetailPage",
            com.tencent.kuikly.core.nvi.serialization.json.JSONObject().apply { put("providerId", item.id) }
        )
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                // TopBar：右上 3 个图标按钮（对齐 Compose actions：✨推荐/📱导入/➕添加）
                NavBar(
                    title = "供应商",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "✨", fontSize = 20f) {
                            KLog.i(UI_LOG, "👆 [Click] $TAG 推荐供应商(✨)")
                            ctx.loadRecommended()
                            ctx.recSheetVisible = true
                        },
                        NavBarAction(label = "📱", fontSize = 20f) {
                            ctx.importFromClipboard()
                        },
                        NavBarAction(label = "＋", fontSize = 24f) {
                            KLog.i(UI_LOG, "👆 [Click] $TAG 添加供应商(＋)")
                            ctx.addSheetVisible = true
                        }
                    )
                )
                // 搜索框（对齐 Compose OutlinedTextField + Search01 leadingIcon）
                View {
                    attr {
                        flexDirectionRow()
                        alignItemsCenter()
                        height(44f)
                        marginLeft(16f)
                        marginRight(16f)
                        marginTop(8f)
                        marginBottom(8f)
                        paddingLeft(12f)
                        paddingRight(12f)
                        borderRadius(22f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                    }
                    Text {
                        attr { text("🔍"); fontSize(14f); marginRight(8f) }
                    }
                    Input {
                        attr {
                            placeholder("搜索供应商...")
                            fontSize(14f)
                            flex(1f)
                        }
                        event {
                            textDidChange {
                                ctx.searchText = it.text
                                ctx.applyFilter()
                            }
                        }
                    }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }
                    vif({ ctx.providers.isEmpty() }) {
                        EmptyView(message = "暂无供应商，点击右上角 ＋ 添加", icon = "🔌")
                    }
                    // 列表（真实数据 + 拖动手柄排序，对齐 Compose ReorderableItem）
                    vfor({ ctx.filteredList }) { item ->
                        val index = ctx.providers.indexOfFirst { it.id == item.id }
                        ReorderableItem(
                            isDragging = { ctx.dragIndex == index },
                            onDragStart = { pageY: Float -> ctx.onDragStart(index, item, pageY) },
                            onDragMove = { pageY: Float -> ctx.onDragMove(pageY) },
                            onDragEnd = { ctx.onDragEnd() }
                        ) {
                            // 列表项主体（点击跳详情）
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    padding(12f)
                                    borderRadius(12f)
                                    backgroundColor(
                                        if (ctx.dragIndex == index) HelixTheme.current.primaryContainer
                                        else if (item.enabled) HelixTheme.current.surfaceContainerLow
                                        else HelixTheme.current.errorContainer
                                    )
                                }
                                event {
                                    click {
                                        if (ctx.dragIndex < 0) { // 拖动中禁用点击
                                            safeRun("$TAG 供应商项[${item.name}]") { ctx.openDetail(item) }
                                        }
                                    }
                                }
                                Text {
                                    attr {
                                        text(if (item.type == "openai") "🤖" else if (item.type == "google") "🔵" else "🟠")
                                        fontSize(22f)
                                        marginRight(10f)
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
                                                if (item.enabled) HelixTheme.current.onSurface
                                                else HelixTheme.current.onErrorContainer
                                            )
                                        }
                                    }
                                    Text {
                                        attr {
                                            text(
                                                (if (item.enabled) "已启用" else "已禁用") +
                                                    " · ${item.modelCount} 个模型 · ${item.type}"
                                            )
                                            fontSize(11f)
                                            color(HelixTheme.current.onSurfaceVariant)
                                            marginTop(2f)
                                        }
                                    }
                                }
                                Text {
                                    attr {
                                        text("›")
                                        fontSize(16f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginLeft(4f)
                                    }
                                }
                            }
                        }
                    }

                // ===== 拖动浮动预览（实时跟手，官方响应式 attr） =====
                DragPreview(
                    visible = { ctx.dragIndex >= 0 },
                    name = { ctx.dragLabel },
                    enabled = { ctx.dragItem?.enabled ?: true },
                    modelCount = { ctx.dragItem?.modelCount ?: 0 },
                    type = { ctx.dragItem?.type ?: "" },
                    pageY = { ctx.dragPageY }
                )
                }

                // ===== 添加方式选择弹层 =====
                OptionSheet(
                    visible = { ctx.addSheetVisible },
                    title = "添加供应商",
                    options = listOf(
                        SheetOption("recommended", "推荐供应商", "常用中转/官方服务一键添加"),
                        SheetOption("custom", "自定义", "手动配置名称/类型/BaseURL/APIKey")
                    ),
                    currentValue = "",
                    onSelect = { o ->
                        ctx.addSheetVisible = false
                        when (o.value) {
                            "recommended" -> {
                                ctx.loadRecommended()
                                ctx.recSheetVisible = true
                            }
                            "custom" -> ctx.customSheetVisible = true
                        }
                    },
                    onDismiss = { ctx.addSheetVisible = false }
                )

                // ===== 推荐供应商弹层 =====
                OptionSheet(
                    visible = { ctx.recSheetVisible },
                    title = "推荐供应商（点击添加）",
                    options = ctx.recProviders.map {
                        SheetOption(
                            it.id,
                            it.name + if (ctx.addedRecIds.contains(it.id)) " ✓已添加" else "",
                            it.baseUrl
                        )
                    },
                    currentValue = "",
                    onSelect = { o ->
                        val rec = ctx.recProviders.firstOrNull { it.id == o.value }
                        if (rec != null) ctx.addRecommended(rec)
                    },
                    onDismiss = { ctx.recSheetVisible = false }
                )

                // ===== 自定义添加弹层 =====
                vif({ ctx.customSheetVisible }) {
                    View {
                        attr {
                            positionAbsolute()
                            top(0f); left(0f); right(0f); bottom(0f)
                            backgroundColor(com.tencent.kuikly.core.base.Color(0x80000000L))
                            justifyContentFlexEnd()
                        }
                        event {
                            click { safeRun("$TAG 自定义弹层遮罩关闭") { ctx.customSheetVisible = false } }
                        }
                        View {
                            attr {
                                alignSelfStretch()
                                padding(16f)
                                borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                                backgroundColor(HelixTheme.current.surfaceContainerHigh)
                            }
                            event { click { safeRun("$TAG 面板阻止穿透") { } } }
                            Text {
                                attr {
                                    text("自定义供应商")
                                    fontSize(16f)
                                    fontWeight700()
                                    color(HelixTheme.current.onSurface)
                                    marginBottom(12f)
                                }
                            }
                            // 类型选择
                            View {
                                attr {
                                    height(44f)
                                    borderRadius(10f)
                                    justifyContentCenter()
                                    paddingLeft(12f)
                                    backgroundColor(HelixTheme.current.surfaceContainerLow)
                                }
                                event {
                                    click {
                                        safeRun("$TAG 类型选择") {
                                            KLog.i(UI_LOG, "👆 [Click] $TAG 自定义类型选择")
                                            ctx.typeSheetVisible = true
                                        }
                                    }
                                }
                                Text {
                                    attr {
                                        text("类型：${ctx.customType}（点击切换）")
                                        fontSize(14f)
                                        color(HelixTheme.current.onSurface)
                                    }
                                }
                            }
                            // 表单（行布局 Input 模式）
                            listOf(
                                "名称（如 My OpenAI）" to { v: String -> ctx.customName = v },
                                "API Base URL（可留空用默认）" to { v: String -> ctx.customBaseUrl = v },
                                "API Key" to { v: String -> ctx.customApiKey = v }
                            ).forEach { (hint, setter) ->
                                var value = ""
                                View {
                                    attr {
                                        flexDirectionRow()
                                        alignItemsCenter()
                                        height(48f)
                                        borderRadius(10f)
                                        marginTop(8f)
                                        paddingLeft(12f)
                                        paddingRight(12f)
                                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                                    }
                                    Input {
                                        attr {
                                            placeholder(hint)
                                            fontSize(14f)
                                            flex(1f)
                                        }
                                        event {
                                            textDidChange { value = it.text; setter(it.text) }
                                        }
                                    }
                                }
                            }
                            // 添加按钮
                            View {
                                attr {
                                    height(44f)
                                    borderRadius(22f)
                                    justifyContentCenter()
                                    alignItemsCenter()
                                    backgroundColor(HelixTheme.current.primary)
                                    marginTop(16f)
                                    marginBottom(16f)
                                }
                                event {
                                    click { safeRun("$TAG 自定义添加提交") { ctx.submitCustom() } }
                                }
                                Text {
                                    attr {
                                        text("添加")
                                        fontSize(15f)
                                        color(HelixTheme.current.onPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // 类型切换弹层
                OptionSheet(
                    visible = { ctx.typeSheetVisible },
                    title = "选择供应商类型",
                    options = listOf(
                        SheetOption("openai", "OpenAI 兼容", "绝大多数中转/官方服务"),
                        SheetOption("google", "Google Gemini", "generativelanguage"),
                        SheetOption("claude", "Anthropic Claude", "messages API")
                    ),
                    currentValue = ctx.customType,
                    onSelect = { o ->
                        ctx.typeSheetVisible = false
                        ctx.customType = o.value
                    },
                    onDismiss = { ctx.typeSheetVisible = false }
                )
            }
        }
    }
}
