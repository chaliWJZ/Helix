package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
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
private const val TAG = "[SettingWebPage]"

/**
 * Web 服务器页（对应 Compose 的 SettingWebPage，真实数据）
 *
 * - pageDidAppear 刷新 getWebServerState
 * - 启停开关（setWebServerEnabled 联动原生 WebServerService 前台服务）
 * - 运行状态/端口/访问地址/安全配置展示
 * - 仅本机访问开关（setWebServerConfig）
 */
@Page("SettingWebPage")
internal class SettingWebPage : HelixBasePage() {

    private var enabled by observable(false)
    private var running by observable(false)
    private var port by observable(8080)
    private var localhostOnly by observable(false)
    private var jwtEnabled by observable(false)
    private var hasPassword by observable(false)
    private var address by observable("")
    private var pwdEditVisible by observable(false)

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新 Web 服务器状态")
        load()
    }

    private fun load() {
        bridge().getWebServerState { json ->
            safeRun("$TAG getWebServerState") {
                val j = json ?: return@safeRun
                enabled = j.optBoolean("enabled")
                running = j.optBoolean("running")
                port = j.optInt("port", 8080)
                localhostOnly = j.optBoolean("localhostOnly")
                jwtEnabled = j.optBoolean("jwtEnabled")
                hasPassword = j.optBoolean("hasPassword")
                address = j.optString("address")
                KLog.i(UI_LOG, "📦 [Data] $TAG 状态 running=$running port=$port address=$address")
            }
        }
    }

    /** 启停（联动原生 Service） */
    private fun toggle(value: Boolean) {
        KLog.i(UI_LOG, "👆 [Click] $TAG Web服务器 → $value")
        bridge().setWebServerEnabled(value) { json ->
            safeRun("$TAG setWebServerEnabled") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    enabled = value
                    load() // 重新拉运行状态
                }
            }
        }
    }

    /** 仅本机访问 */
    private fun toggleLocalhost(value: Boolean) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 仅本机访问 → $value")
        bridge().setWebServerConfig(localhostOnly = value) { json ->
            safeRun("$TAG setWebServerConfig") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) localhostOnly = value
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
                NavBar(title = "Web 服务器", showBack = true, onBack = { ctx.closePage() })
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }

                    // ===== 启停开关 =====
                    View {
                        attr {
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(12f)
                            borderRadius(12f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        SwitchRow(
                            title = "启用 Web 服务器",
                            subtitle = if (ctx.running) "运行中：http://${if (ctx.address.isNotEmpty()) ctx.address else "localhost"}:${ctx.port}"
                                       else "在浏览器中访问 Helix",
                            checked = { ctx.enabled },
                            onCheckedChange = { ctx.toggle(it) }
                        )
                    }

                    // ===== 运行状态 =====
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "运行状态",
                                subtitle = if (ctx.running) "✅ 运行中" else "⏹ 已停止",
                                icon = "🖥️"
                            ),
                            CardGroupItem(
                                title = "端口",
                                subtitle = "${ctx.port}",
                                icon = "🔢"
                            ),
                            CardGroupItem(
                                title = "访问地址",
                                subtitle = if (ctx.address.isNotEmpty()) "http://${ctx.address}:${ctx.port}" else "http://localhost:${ctx.port}",
                                icon = "🌐"
                            ),
                            CardGroupItem(
                                title = "访问密码",
                                subtitle = if (ctx.hasPassword) "已设置（点击修改）" else "未设置（点击配置）",
                                icon = "🔑",
                                onClick = { ctx.pwdEditVisible = true }
                            )
                        )
                    )

                    // ===== 安全选项 =====
                    View {
                        attr {
                            marginLeft(16f)
                            marginRight(16f)
                            marginTop(8f)
                            marginBottom(32f)
                            borderRadius(12f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        SwitchRow(
                            title = "仅本机访问",
                            subtitle = "开启后只能从本设备浏览器访问",
                            checked = { ctx.localhostOnly },
                            onCheckedChange = { ctx.toggleLocalhost(it) }
                        )
                    }
                }

                // 密码编辑弹层
                EditSheet(
                    visible = { ctx.pwdEditVisible },
                    title = "设置访问密码",
                    label = "密码（浏览器访问时需要输入）",
                    initialValue = "",
                    onSave = { v: String ->
                        ctx.pwdEditVisible = false
                        KLog.i(UI_LOG, "👆 [Click] $TAG 设置访问密码 len=${v.length}")
                        ctx.bridge().setWebServerConfig(password = v) { json ->
                            safeRun("$TAG setWebServerConfig password") {
                                if (json?.optBoolean("ok") == true) {
                                    ctx.hasPassword = v.isNotEmpty()
                                    ctx.bridge().toast("密码已保存")
                                }
                            }
                        }
                    },
                    onDismiss = { ctx.pwdEditVisible = false }
                )
            }
        }
    }
}
