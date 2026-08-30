package com.helix.kuikly.pages.log

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.NavBarAction
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[LogPage]"

/** 列表项（卡片字段，大 body 不在此层传输） */
internal data class LogItem(
    val id: String,
    val type: String,          // "text" | "request"
    val timeText: String,
    val tag: String,
    val message: String = "",
    val url: String = "",
    val method: String = "",
    val responseCode: Int = 0,
    val durationMs: Long = 0L,
    val error: String = ""
)

/** 详情弹层数据（getLogDetail 按需拉取） */
internal data class LogDetail(
    val timeFull: String,
    val url: String,
    val method: String,
    val responseCode: Int,
    val durationMs: Long,
    val error: String,
    val requestHeaders: List<Pair<String, String>>,
    val responseHeaders: List<Pair<String, String>>,
    val requestBody: String
)

/**
 * 请求日志页（对应 Compose 的 LogPage，真实数据）
 *
 * 数据源：原生 Logging 单例（与 Compose 版同一份内存日志）。
 * - 请求日志开关（SwitchRow → setRequestLogging 桥）
 * - 日志列表（TextLog / RequestLog 两类卡片，vfor 渲染）
 * - 点击 RequestLog → getLogDetail 桥 → 底部详情弹层（headers/body/复制）
 * - NavBar 右侧清空按钮（clearLogs 桥）
 */
@Page("LogPage")
internal class LogPage : HelixBasePage() {

    private val logs by observableList<LogItem>()
    private var requestLoggingEnabled by observable(false)
    private var showDetail by observable(false)
    private var detail by observable<LogDetail?>(null)

    override fun created() {
        super.created()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建，拉取日志列表")
        loadLogs()
    }

    /** 拉取日志列表 + 开关状态 */
    private fun loadLogs() {
        bridge().getLogs { json ->
            safeRun("$TAG loadLogs") {
                val j = json ?: run {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG getLogs 回调为空")
                    return@safeRun
                }
                val enabled = j.optBoolean("enabled")
                val arr = j.optJSONArray("logs")
                val items = mutableListOf<LogItem>()
                arr?.let { a ->
                    for (i in 0 until a.length()) {
                        a.optJSONObject(i)?.let { o ->
                            items.add(
                                LogItem(
                                    id = o.optString("id"),
                                    type = o.optString("type"),
                                    timeText = o.optString("timeText"),
                                    tag = o.optString("tag"),
                                    message = o.optString("message"),
                                    url = o.optString("url"),
                                    method = o.optString("method"),
                                    responseCode = o.optInt("responseCode"),
                                    durationMs = o.optLong("durationMs"),
                                    error = o.optString("error")
                                )
                            )
                        }
                    }
                }
                requestLoggingEnabled = enabled
                logs.clear()
                logs.addAll(items)
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 条日志, 请求记录开关=$enabled")
            }
        }
    }

    /** 切换请求日志开关 */
    private fun toggleRequestLogging(enabled: Boolean) {
        bridge().setRequestLoggingEnabled(enabled) { json ->
            safeRun("$TAG setRequestLogging") {
                val j = json ?: return@safeRun
                val ok = j.optBoolean("ok")
                if (ok) requestLoggingEnabled = j.optBoolean("enabled", enabled)
                KLog.i(UI_LOG, "✅ [Bridge] $TAG 请求记录开关 → $requestLoggingEnabled (ok=$ok)")
            }
        }
    }

    /** 清空日志 */
    private fun clearAllLogs() {
        bridge().clearLogs { json ->
            safeRun("$TAG clearLogs") {
                val j = json ?: return@safeRun
                val ok = j.optBoolean("ok")
                if (ok) loadLogs()
                KLog.i(UI_LOG, "🧹 [Bridge] $TAG 清空日志 (ok=$ok)")
            }
        }
    }

    /** 点击请求日志卡片 → 拉详情并弹层 */
    private fun openDetail(item: LogItem) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 查看请求详情 ${item.method} ${item.url.take(60)}")
        bridge().getLogDetail(item.id) { json ->
            safeRun("$TAG getLogDetail") {
                val j = json ?: run {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG getLogDetail 回调为空")
                    return@safeRun
                }
                if (j.optString("error").isNotEmpty()) {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 详情拉取失败: ${j.optString("error")}")
                    return@safeRun
                }
                fun headersOf(name: String): List<Pair<String, String>> {
                    val list = mutableListOf<Pair<String, String>>()
                    j.optJSONObject(name)?.let { obj ->
                        obj.keySet().forEach { k -> list.add(k to obj.optString(k)) }
                    }
                    return list
                }
                val d = LogDetail(
                    timeFull = j.optString("timeFull"),
                    url = j.optString("url"),
                    method = j.optString("method"),
                    responseCode = j.optInt("responseCode"),
                    durationMs = j.optLong("durationMs"),
                    error = j.optString("error"),
                    requestHeaders = headersOf("requestHeaders"),
                    responseHeaders = headersOf("responseHeaders"),
                    requestBody = j.optString("requestBody")
                )
                detail = d
                showDetail = true
                KLog.i(UI_LOG, "📦 [Data] $TAG 详情已加载, headers=${d.requestHeaders.size}+${d.responseHeaders.size}")
            }
        }
    }

    /** 复制请求体到剪贴板 */
    private fun copyBody(body: String) {
        bridge().copyToClipboard(body) { json ->
            safeRun("$TAG copyToClipboard") {
                val j = json ?: return@safeRun
                val ok = j.optBoolean("ok")
                KLog.i(UI_LOG, "📋 [Bridge] $TAG 复制请求体 ${body.length} 字符 (ok=$ok)")
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
                    title = "请求日志",
                    showBack = true,
                    onBack = { ctx.closePage() },
                    rightActions = listOf(
                        NavBarAction(label = "清空") { ctx.clearAllLogs() }
                    )
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }

                    // ===== 请求记录开关卡 =====
                    View {
                        attr {
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(12f)
                            borderRadius(12f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        SwitchRow(
                            title = "记录网络请求",
                            subtitle = "开启后捕获 AI 供应商等 HTTP 请求，用于调试",
                            checked = { ctx.requestLoggingEnabled },
                            onCheckedChange = { ctx.toggleRequestLogging(it) }
                        )
                    }

                    // ===== 空态 =====
                    if (ctx.logs.isEmpty()) {
                        Text {
                            attr {
                                text("暂无日志\n（开启请求记录后，发起一次对话即可看到请求）")
                                fontSize(13f)
                                color(HelixTheme.current.onSurfaceVariant)
                                alignSelfCenter()
                                marginTop(48f)
                                textAlignCenter()
                            }
                        }
                    }

                    // ===== 日志列表 =====
                    vfor({ ctx.logs }) { item ->
                        if (item.type == "request") {
                            RequestLogCard(item) { ctx.openDetail(item) }
                        } else {
                            TextLogCard(item)
                        }
                    }
                }

                // ===== 详情弹层（遮罩 + 底部面板） =====
                if (ctx.showDetail) {
                    val d = ctx.detail
                    if (d != null) {
                        View {
                            attr {
                                positionAbsolute()
                                top(0f)
                                left(0f)
                                right(0f)
                                bottom(0f)
                                backgroundColor(Color(0x80000000L))
                                justifyContentFlexEnd()
                            }
                            event {
                                click {
                                    safeRun("$TAG 关闭详情遮罩") {
                                        KLog.i(UI_LOG, "👆 [Click] $TAG 关闭详情")
                                        ctx.showDetail = false
                                    }
                                }
                            }
                            // 底部面板（点击不冒泡关闭）
                            View {
                                attr {
                                    alignSelfStretch()
                                    height(460f)
                                    borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                                }
                                Text {
                                    attr {
                                        text("Request Details")
                                        fontSize(16f)
                                        fontWeight700()
                                        color(HelixTheme.current.onSurface)
                                        marginLeft(16f)
                                        marginTop(16f)
                                        marginBottom(8f)
                                    }
                                }
                                Scroller {
                                    attr {
                                        flex(1f)
                                        alignSelfStretch()
                                        marginBottom(16f)
                                    }
                                    DetailField("Time", d.timeFull)
                                    DetailField("URL", d.url)
                                    DetailField("Method", d.method)
                                    if (d.responseCode > 0) DetailField("Status Code", "${d.responseCode}")
                                    if (d.durationMs > 0) DetailField("Duration", "${d.durationMs}ms")
                                    if (d.error.isNotEmpty()) DetailField("Error", d.error)

                                    if (d.requestHeaders.isNotEmpty()) {
                                        SectionLabel("Request Headers")
                                        d.requestHeaders.forEach { (k, v) -> HeaderField(k, v) }
                                    }
                                    if (d.requestBody.isNotEmpty()) {
                                        SectionLabel("Request Body")
                                        View {
                                            attr { flexDirectionRow() }
                                            Text {
                                                attr {
                                                    text(d.requestBody)
                                                    fontSize(11f)
                                                    fontFamily("monospace")
                                                    color(HelixTheme.current.onSurface)
                                                    flex(1f)
                                                    marginLeft(16f)
                                                }
                                            }
                                            Text {
                                                attr {
                                                    text("复制")
                                                    fontSize(12f)
                                                    color(HelixTheme.current.primary)
                                                    marginRight(16f)
                                                    marginLeft(8f)
                                                    marginTop(8f)
                                                }
                                            }
                                            event {
                                                click {
                                                    safeRun("$TAG 复制请求体") {
                                                        KLog.i(UI_LOG, "👆 [Click] $TAG 复制请求体按钮")
                                                        ctx.copyBody(d.requestBody)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (d.responseHeaders.isNotEmpty()) {
                                        SectionLabel("Response Headers")
                                        d.responseHeaders.forEach { (k, v) -> HeaderField(k, v) }
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

// ==================== 顶层子组件（DSL 内可直呼） ====================

private fun statusColor(code: Int): Color =
    if (code in 200..299) HelixTheme.current.primary else Color(0xFFE53935L)

/** 请求日志卡片（method + 时间 / URL / 状态与耗时 / 错误） */
private fun ViewContainer<*, *>.RequestLogCard(item: LogItem, onClick: () -> Unit) {
    View {
        attr {
            flexDirectionColumn()
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
            padding(12f)
            borderRadius(12f)
            backgroundColor(HelixTheme.current.surfaceContainerLow)
        }
        event {
            click {
                safeRun("$TAG RequestLogCard") { onClick() }
            }
        }
        // method + 时间
        View {
            attr {
                flexDirectionRow()
                justifyContentSpaceBetween()
                alignSelfStretch()
                marginBottom(4f)
            }
            Text {
                attr {
                    text(item.method)
                    fontSize(12f)
                    fontWeight700()
                    color(HelixTheme.current.primary)
                }
            }
            Text {
                attr {
                    text(item.timeText)
                    fontSize(10f)
                    color(HelixTheme.current.onSurfaceVariant)
                }
            }
        }
        // URL
        Text {
            attr {
                text(item.url)
                fontSize(11f)
                fontFamily("monospace")
                color(HelixTheme.current.onSurface)
                marginBottom(4f)
            }
        }
        // 状态 + 耗时
        if (item.responseCode > 0 || item.durationMs > 0) {
            View {
                attr {
                    flexDirectionRow()
                    marginBottom(4f)
                }
                if (item.responseCode > 0) {
                    Text {
                        attr {
                            text("Status: ${item.responseCode}   ")
                            fontSize(10f)
                            color(statusColor(item.responseCode))
                        }
                    }
                }
                if (item.durationMs > 0) {
                    Text {
                        attr {
                            text("${item.durationMs}ms")
                            fontSize(10f)
                            color(HelixTheme.current.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        // 错误
        if (item.error.isNotEmpty()) {
            Text {
                attr {
                    text("Error: ${item.error}")
                    fontSize(10f)
                    color(Color(0xFFE53935L))
                }
            }
        }
    }
}

/** 文本日志卡片（tag + 时间 / 消息） */
private fun ViewContainer<*, *>.TextLogCard(item: LogItem) {
    View {
        attr {
            flexDirectionColumn()
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
            padding(12f)
            borderRadius(12f)
            backgroundColor(HelixTheme.current.surfaceContainerLow)
        }
        View {
            attr {
                flexDirectionRow()
                justifyContentSpaceBetween()
                alignSelfStretch()
                marginBottom(4f)
            }
            Text {
                attr {
                    text(item.tag)
                    fontSize(10f)
                    fontWeight700()
                    color(HelixTheme.current.primary)
                }
            }
            Text {
                attr {
                    text(item.timeText)
                    fontSize(10f)
                    color(HelixTheme.current.onSurfaceVariant)
                }
            }
        }
        Text {
            attr {
                text(item.message)
                fontSize(11f)
                fontFamily("monospace")
                color(HelixTheme.current.onSurface)
            }
        }
    }
}

/** 详情字段（label + value） */
private fun ViewContainer<*, *>.DetailField(label: String, value: String) {
    View {
        attr {
            flexDirectionColumn()
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
        }
        Text {
            attr {
                text(label)
                fontSize(10f)
                color(HelixTheme.current.onSurfaceVariant)
            }
        }
        Text {
            attr {
                text(value)
                fontSize(12f)
                fontFamily("monospace")
                color(HelixTheme.current.onSurface)
            }
        }
    }
}

/** 详情分区标题 */
private fun ViewContainer<*, *>.SectionLabel(title: String) {
    Text {
        attr {
            text(title)
            fontSize(12f)
            fontWeight700()
            color(HelixTheme.current.onSurface)
            marginLeft(16f)
            marginTop(12f)
            marginBottom(6f)
        }
    }
}

/** header 键值对 */
private fun ViewContainer<*, *>.HeaderField(key: String, value: String) {
    View {
        attr {
            flexDirectionColumn()
            marginLeft(16f)
            marginRight(16f)
            marginBottom(4f)
        }
        Text {
            attr {
                text(key)
                fontSize(10f)
                color(HelixTheme.current.primary)
            }
        }
        Text {
            attr {
                text(value)
                fontSize(10f)
                fontFamily("monospace")
                color(HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}
