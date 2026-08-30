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
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.OptionSheet
import com.helix.kuikly.components.ui.SheetOption
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingProviderPage]"

internal data class ProviderItem(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val modelCount: Int,
    val type: String
)

internal data class RecProvider(val id: String, val name: String, val type: String, val baseUrl: String)

/**
 * 供应商列表页（对齐 Compose：搜索过滤 + 推荐一键添加 + 自定义添加 + 排序 + 富化展示）
 *
 * - pageDidAppear 刷新（详情页改动能同步回来）
 * - 顶栏「添加」→ 弹层选：推荐供应商一键添加 / 自定义添加（名称/类型/BaseURL/APIKey）
 * - 搜索框实时过滤（本地）
 * - 列表项：启用状态/模型数/类型 + 上移/下移 + 点击跳详情传 providerId
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

    /** 应用搜索过滤（本地） */
    private fun applyFilter() {
        val list = if (searchText.isEmpty()) providers.toList()
        else providers.filter { it.name.contains(searchText, true) || it.type.contains(searchText, true) }
        filteredList.clear()
        filteredList.addAll(list)
        KLog.i(UI_LOG, "📦 [Data] $TAG 过滤结果 ${list.size}/${providers.size} (关键词=${searchText})")
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
                            items.add(
                                RecProvider(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    type = o.optString("type"),
                                    baseUrl = o.optString("baseUrl")
                                )
                            )
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

    private fun move(index: Int, to: Int) {
        if (to !in providers.indices) return
        bridge().moveProvider(index, to) { json ->
            safeRun("$TAG moveProvider") {
                if (json?.optBoolean("ok") == true) loadProviders()
            }
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
                NavBar(
                    title = "供应商",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("添加") {
                        KLog.i(UI_LOG, "👆 [Click] $TAG 添加供应商")
                        ctx.addSheetVisible = true
                    })
                )
                // 搜索框
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
                        attr {
                            text("🔍")
                            fontSize(14f)
                            marginRight(8f)
                        }
                    }
                    Input {
                        attr {
                            placeholder("搜索供应商名称/类型...")
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
                        EmptyView(message = "暂无供应商，点击右上角「添加」", icon = "🔌")
                    }
                    vfor({ ctx.filteredList }) { item ->
                        val index = ctx.providers.indexOfFirst { it.id == item.id }
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
                                    if (item.enabled) HelixTheme.current.surfaceContainerLow
                                    else HelixTheme.current.errorContainer
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG 供应商项[${item.name}]") { ctx.openDetail(item) }
                                }
                            }
                            // 图标 + 信息
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
                            // 上移/下移
                            Text {
                                attr {
                                    text("↑"); fontSize(13f); color(HelixTheme.current.primary)
                                    marginLeft(6f); marginRight(2f); marginTop(6f); marginBottom(6f)
                                }
                                event {
                                    click { safeRun("$TAG 上移[${item.name}]") { ctx.move(index, index - 1) } }
                                }
                            }
                            Text {
                                attr {
                                    text("↓"); fontSize(13f); color(HelixTheme.current.primary)
                                    marginLeft(2f); marginRight(4f); marginTop(6f); marginBottom(6f)
                                }
                                event {
                                    click { safeRun("$TAG 下移[${item.name}]") { ctx.move(index, index + 1) } }
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

                // ===== 推荐供应商弹层（标记已添加） =====
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
                            click {
                                safeRun("$TAG 自定义弹层遮罩关闭") { ctx.customSheetVisible = false }
                            }
                        }
                        View {
                            attr {
                                alignSelfStretch()
                                padding(16f)
                                borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                                backgroundColor(HelixTheme.current.surfaceContainerHigh)
                            }
                            event {
                                click { safeRun("$TAG 面板阻止穿透") { } }
                            }
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
                                    click {
                                        safeRun("$TAG 自定义添加提交") { ctx.submitCustom() }
                                    }
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

                // 类型切换弹层（自定义添加用）
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
