package com.helix.kuikly.pages.backup

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
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[BackupPage]"

/**
 * 数据备份页（真实数据版，双 Tab：WebDav / S3）
 *
 * - WebDav Tab：URL/用户名/密码/路径 逐项编辑 + 测试连接 + 立即备份（走原生 WebDavSync）
 * - S3 Tab：endpoint/accessKey/secretKey/bucket/region 编辑 + 立即备份（走原生 S3Sync）
 * - 导入导出/备份提醒/备份文件列表恢复 在 Compose 版操作
 */
@Page("BackupPage")
internal class BackupPage : HelixBasePage() {

    // WebDav
    private var wdUrl by observable("")
    private var wdUsername by observable("")
    private var wdPassword by observable("")
    private var wdPath by observable("helix_backups")
    private var wdConfigured by observable(false)
    // S3
    private var s3Endpoint by observable("")
    private var s3AccessKey by observable("")
    private var s3SecretKey by observable("")
    private var s3Bucket by observable("")
    private var s3Region by observable("auto")
    private var s3Configured by observable(false)

    private var currentTab by observable(0) // 0=WebDav 1=S3
    private var editVisible by observable(false)
    private var editField by observable("")
    private var busy by observable(false)

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面出现，刷新备份配置")
        load()
    }

    private fun load() {
        bridge().getBackupConfig { json ->
            safeRun("$TAG getBackupConfig") {
                val j = json ?: return@safeRun
                j.optJSONObject("webdav")?.let { w ->
                    wdUrl = w.optString("url")
                    wdUsername = w.optString("username")
                    wdPassword = w.optString("password")
                    wdPath = w.optString("path")
                    wdConfigured = w.optBoolean("configured")
                }
                j.optJSONObject("s3")?.let { s ->
                    s3Endpoint = s.optString("endpoint")
                    s3AccessKey = s.optString("accessKeyId")
                    s3SecretKey = s.optString("secretAccessKey")
                    s3Bucket = s.optString("bucket")
                    s3Region = s.optString("region")
                    s3Configured = s.optBoolean("configured")
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG webdav=$wdConfigured s3=$s3Configured")
            }
        }
    }

    private fun saveWebDav(field: String, value: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存WebDav[$field]")
        bridge().updateWebDavConfig(
            url = if (field == "url") value else null,
            username = if (field == "username") value else null,
            password = if (field == "password") value else null,
            path = if (field == "path") value else null
        ) { json ->
            safeRun("$TAG updateWebDavConfig") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "url" -> wdUrl = value
                        "username" -> wdUsername = value
                        "password" -> wdPassword = value
                        "path" -> wdPath = value
                    }
                    load()
                    bridge().toast("保存成功")
                } else bridge().toast("保存失败")
            }
        }
    }

    private fun saveS3(field: String, value: String) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存S3[$field]")
        bridge().updateS3Config(
            endpoint = if (field == "endpoint") value else null,
            accessKeyId = if (field == "accessKey") value else null,
            secretAccessKey = if (field == "secretKey") value else null,
            bucket = if (field == "bucket") value else null,
            region = if (field == "region") value else null
        ) { json ->
            safeRun("$TAG updateS3Config") {
                if (json?.optBoolean("ok") == true) {
                    when (field) {
                        "endpoint" -> s3Endpoint = value
                        "accessKey" -> s3AccessKey = value
                        "secretKey" -> s3SecretKey = value
                        "bucket" -> s3Bucket = value
                        "region" -> s3Region = value
                    }
                    load()
                    bridge().toast("保存成功")
                } else bridge().toast("保存失败")
            }
        }
    }

    private fun testWebDav() {
        if (busy) return
        busy = true
        bridge().toast("测试中...")
        bridge().testWebDav { json ->
            safeRun("$TAG testWebDav") {
                busy = false
                if (json?.optBoolean("ok") == true) bridge().toast("✅ 连接成功")
                else bridge().toast("❌ ${json?.optString("error")?.take(50) ?: "连接失败"}")
            }
        }
    }

    private fun backup(kind: String) {
        if (busy) return
        busy = true
        bridge().toast("备份中...")
        val cb = { json: com.tencent.kuikly.core.nvi.serialization.json.JSONObject? ->
            safeRun("$TAG backup$kind") {
                busy = false
                if (json?.optBoolean("ok") == true) bridge().toast("✅ 备份成功")
                else bridge().toast("❌ ${json?.optString("error")?.take(60) ?: "备份失败"}")
            }
        }
        if (kind == "WebDav") bridge().backupWebDav(cb) else bridge().backupS3(cb)
    }

    private fun editArgs(): Triple<String, String, String> = when (editField) {
        "url" -> Triple("编辑 WebDav 地址", "https://dav.example.com/dav/", wdUrl)
        "username" -> Triple("编辑用户名", "用户名", wdUsername)
        "password" -> Triple("编辑密码", "密码", "")
        "path" -> Triple("编辑备份路径", "远端目录", wdPath)
        "endpoint" -> Triple("编辑 S3 Endpoint", "https://s3.example.com", s3Endpoint)
        "accessKey" -> Triple("编辑 Access Key ID", "AccessKeyId", s3AccessKey)
        "secretKey" -> Triple("编辑 Secret Access Key", "SecretAccessKey", "")
        "bucket" -> Triple("编辑 Bucket", "存储桶名", s3Bucket)
        "region" -> Triple("编辑 Region", "区域（如 auto）", s3Region)
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
                NavBar(title = "数据备份", showBack = true, onBack = { ctx.closePage() })
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
                    TabBtn("WebDav", ctx.currentTab == 0) { ctx.currentTab = 0 }
                    TabBtn("S3", ctx.currentTab == 1) { ctx.currentTab = 1 }
                }
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                    }

                    // ===== WebDav Tab =====
                    vif({ ctx.currentTab == 0 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "服务器地址", subtitle = ctx.wdUrl.ifEmpty { "未配置" }, icon = "🌐", onClick = {
                                    ctx.editField = "url"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "用户名", subtitle = ctx.wdUsername.ifEmpty { "未配置" }, icon = "👤", onClick = {
                                    ctx.editField = "username"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "密码", subtitle = if (ctx.wdPassword.isEmpty()) "未配置" else "已配置（点击修改）", icon = "🔑", onClick = {
                                    ctx.editField = "password"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "备份路径", subtitle = ctx.wdPath, icon = "📁", onClick = {
                                    ctx.editField = "path"; ctx.editVisible = true
                                })
                            )
                        )
                        // 测试连接
                        View {
                            attr {
                                height(44f)
                                borderRadius(22f)
                                justifyContentCenter()
                                alignItemsCenter()
                                marginLeft(16f); marginRight(16f)
                                marginTop(8f)
                                backgroundColor(HelixTheme.current.secondaryContainer)
                            }
                            event {
                                click { safeRun("$TAG 测试WebDav连接") { ctx.testWebDav() } }
                            }
                            Text {
                                attr {
                                    text("测试连接")
                                    fontSize(15f)
                                    color(HelixTheme.current.onSecondaryContainer)
                                }
                            }
                        }
                        // 立即备份
                        View {
                            attr {
                                height(44f)
                                borderRadius(22f)
                                justifyContentCenter()
                                alignItemsCenter()
                                marginLeft(16f); marginRight(16f)
                                marginTop(8f)
                                backgroundColor(
                                    if (ctx.wdConfigured) HelixTheme.current.primary else HelixTheme.current.surfaceContainerHighest
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG WebDav立即备份") {
                                        if (!ctx.wdConfigured) {
                                            ctx.bridge().toast("请先配置服务器地址和用户名")
                                        } else ctx.backup("WebDav")
                                    }
                                }
                            }
                            Text {
                                attr {
                                    text(if (ctx.wdConfigured) "立即备份到 WebDav" else "未配置（点击上方项配置）")
                                    fontSize(15f)
                                    color(
                                        if (ctx.wdConfigured) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text {
                            attr {
                                text("备份文件列表/恢复/自动备份 在 Compose 版操作")
                                fontSize(11f)
                                color(HelixTheme.current.onSurfaceVariant)
                                marginTop(12f)
                                marginBottom(16f)
                                alignSelfCenter()
                            }
                        }
                    }

                    // ===== S3 Tab =====
                    vif({ ctx.currentTab == 1 }) {
                        CardGroup(
                            items = listOf(
                                CardGroupItem(title = "Endpoint", subtitle = ctx.s3Endpoint.ifEmpty { "未配置" }, icon = "🌐", onClick = {
                                    ctx.editField = "endpoint"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Access Key ID", subtitle = ctx.s3AccessKey.ifEmpty { "未配置" }, icon = "🪪", onClick = {
                                    ctx.editField = "accessKey"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Secret Access Key", subtitle = if (ctx.s3SecretKey.isEmpty()) "未配置" else "已配置（点击修改）", icon = "🔑", onClick = {
                                    ctx.editField = "secretKey"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Bucket", subtitle = ctx.s3Bucket.ifEmpty { "未配置" }, icon = "🪣", onClick = {
                                    ctx.editField = "bucket"; ctx.editVisible = true
                                }),
                                CardGroupItem(title = "Region", subtitle = ctx.s3Region, icon = "📍", onClick = {
                                    ctx.editField = "region"; ctx.editVisible = true
                                })
                            )
                        )
                        View {
                            attr {
                                height(44f)
                                borderRadius(22f)
                                justifyContentCenter()
                                alignItemsCenter()
                                marginLeft(16f); marginRight(16f)
                                marginTop(8f)
                                marginBottom(32f)
                                backgroundColor(
                                    if (ctx.s3Configured) HelixTheme.current.primary else HelixTheme.current.surfaceContainerHighest
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG S3立即备份") {
                                        if (!ctx.s3Configured) {
                                            ctx.bridge().toast("请先配置 Endpoint 和 Bucket")
                                        } else ctx.backup("S3")
                                    }
                                }
                            }
                            Text {
                                attr {
                                    text(if (ctx.s3Configured) "立即备份到 S3" else "未配置（点击上方项配置）")
                                    fontSize(15f)
                                    color(
                                        if (ctx.s3Configured) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant
                                    )
                                }
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
                    maskDisplay = ctx.editField == "password" || ctx.editField == "secretKey",
                    onSave = { value: String ->
                        ctx.editVisible = false
                        if (ctx.currentTab == 0) ctx.saveWebDav(ctx.editField, value)
                        else ctx.saveS3(ctx.editField, value)
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
                fontSize(14f)
                fontWeight700()
                color(if (selected) HelixTheme.current.onPrimary else HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}
