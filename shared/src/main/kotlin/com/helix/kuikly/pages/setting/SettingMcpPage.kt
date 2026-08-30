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
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingMcpPage]"

internal data class McpItem(
    val id: String,
    val name: String,
    val url: String,
    val type: String,
    val enable: Boolean
)

/**
 * MCP 设置页（对应 Compose 的 SettingMcpPage，真实数据）
 *
 * - pageDidAppear 刷新 getMcpServers
 * - 列表：名称 + URL + 类型，每项启用开关（setMcpEnabled 持久化）
 * - 删除（deleteMcpServer）
 */
@Page("SettingMcpPage")
internal class SettingMcpPage : HelixBasePage() {

    private val servers by observableList<McpItem>()
    // 添加流程：名称 → URL → 类型
    private var nameSheetVisible by observable(false)
    private var urlSheetVisible by observable(false)
    private var typeSheetVisible by observable(false)
    private var newName by observable("")
    private var newUrl by observable("")

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新 MCP 列表")
        load()
    }

    private fun load() {
        bridge().getMcpServers { json ->
            safeRun("$TAG getMcpServers") {
                val j = json ?: return@safeRun
                val arr = j.optJSONArray("servers")
                val items = mutableListOf<McpItem>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(
                                McpItem(
                                    id = o.optString("id"),
                                    name = o.optString("name"),
                                    url = o.optString("url"),
                                    type = o.optString("type"),
                                    enable = o.optBoolean("enable")
                                )
                            )
                        }
                    }
                }
                servers.clear()
                servers.addAll(items)
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 个 MCP 服务器")
            }
        }
    }

    private fun toggle(item: McpItem, enable: Boolean) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 启停 [${item.name}] → $enable")
        bridge().setMcpEnabled(item.id, enable) { json ->
            safeRun("$TAG setMcpEnabled") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) load()
            }
        }
    }

    private fun delete(item: McpItem) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 删除 [${item.name}]")
        bridge().deleteMcpServer(item.id) { json ->
            safeRun("$TAG deleteMcpServer") {
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
                    title = "MCP",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(NavBarAction("添加") {
                        KLog.i(UI_LOG, "👆 [Click] $TAG 添加MCP服务器")
                        ctx.newName = ""; ctx.newUrl = ""
                        ctx.nameSheetVisible = true
                    })
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    vif({ ctx.servers.isEmpty() }) {
                        EmptyView(message = "暂无 MCP 服务器，在 Compose 版添加后同步显示", icon = "🔗")
                    }
                    vfor({ ctx.servers }) { item ->
                        View {
                            attr {
                                flexDirectionColumn()
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                padding(12f)
                                borderRadius(12f)
                                backgroundColor(
                                    if (item.enable) HelixTheme.current.surfaceContainerLow
                                    else HelixTheme.current.errorContainer
                                )
                            }
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                }
                                View {
                                    attr { flex(1f) }
                                    Text {
                                        attr {
                                            text(item.name)
                                            fontSize(15f)
                                            fontWeight700()
                                            color(
                                                if (item.enable) HelixTheme.current.onSurface
                                                else HelixTheme.current.onErrorContainer
                                            )
                                        }
                                    }
                                    Text {
                                        attr {
                                            text("${item.type} · ${item.url}")
                                            fontSize(10f)
                                            fontFamily("monospace")
                                            color(HelixTheme.current.onSurfaceVariant)
                                            marginTop(2f)
                                        }
                                    }
                                }
                                // 删除按钮
                                Text {
                                    attr {
                                        text("删除")
                                        fontSize(12f)
                                        color(HelixTheme.current.error)
                                        marginLeft(8f)
                                        marginRight(4f)
                                        marginTop(8f)
                                        marginBottom(8f)
                                    }
                                    event {
                                        click {
                                            safeRun("$TAG 删除[${item.name}]") { ctx.delete(item) }
                                        }
                                    }
                                }
                            }
                            // 启用开关
                            SwitchRow(
                                title = "启用",
                                checked = { item.enable },
                                onCheckedChange = { ctx.toggle(item, it) }
                            )
                        }
                    }
                }

                // ===== 添加流程弹层 =====
                EditSheet(
                    visible = { ctx.nameSheetVisible },
                    title = "新建 MCP 服务器（1/3）",
                    label = "名称",
                    initialValue = ctx.newName,
                    onSave = { v: String ->
                        ctx.newName = v
                        ctx.nameSheetVisible = false
                        ctx.urlSheetVisible = true
                    },
                    onDismiss = { ctx.nameSheetVisible = false }
                )
                EditSheet(
                    visible = { ctx.urlSheetVisible },
                    title = "新建 MCP 服务器（2/3）",
                    label = "服务器 URL",
                    initialValue = ctx.newUrl,
                    onSave = { v: String ->
                        ctx.newUrl = v
                        ctx.urlSheetVisible = false
                        ctx.typeSheetVisible = true
                    },
                    onDismiss = { ctx.urlSheetVisible = false }
                )
                OptionSheet(
                    visible = { ctx.typeSheetVisible },
                    title = "新建 MCP 服务器（3/3）：连接类型",
                    options = listOf(
                        SheetOption("sse", "SSE", "Server-Sent Events 传统模式"),
                        SheetOption("streamable_http", "Streamable HTTP", "新版 MCP 传输协议")
                    ),
                    currentValue = "sse",
                    onSelect = { o ->
                        ctx.typeSheetVisible = false
                        ctx.bridge().addMcpServer(ctx.newName, ctx.newUrl, o.value) { json ->
                            safeRun("$TAG addMcpServer") {
                                if (json?.optBoolean("ok") == true) {
                                    ctx.bridge().toast("已添加 ${ctx.newName}")
                                    ctx.load()
                                } else ctx.bridge().toast(json?.optString("error") ?: "添加失败")
                            }
                        }
                    },
                    onDismiss = { ctx.typeSheetVisible = false }
                )
            }
        }
    }
}
