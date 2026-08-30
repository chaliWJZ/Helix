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
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SwitchRow
import com.helix.kuikly.components.ui.safeRun
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

private const val UI_LOG = "HelixKuikly"
private const val TAG = "[SettingProviderDetailPage]"

internal data class ProviderModelItem(val modelId: String, val displayName: String, val type: String)

/**
 * 供应商详情页面（对应 Compose 的 SettingProviderDetailPage，真实数据）
 *
 * - 读 pageData.params.providerId → getProviderDetail 桥
 * - 表单：名称/BaseURL/API Key（Input 可编辑）+ 保存（updateProvider 桥）
 * - 启用开关（setProviderEnabled）/ 测试连通性（testProvider→listModels）/ 删除（deleteProvider）
 * - 模型列表（只读展示）
 */
@Page("SettingProviderDetailPage")
internal class SettingProviderDetailPage : HelixBasePage() {

    private var providerId by observable("")
    private var providerName by observable("")
    private var providerType by observable("")
    private var enabled by observable(true)
    private val models by observableList<ProviderModelItem>()

    // 表单编辑值
    private var nameInput by observable("")
    private var baseUrlInput by observable("")
    private var apiKeyInput by observable("")

    // 测试状态
    private var testing by observable(false)
    private var testResult by observable("")

    override fun created() {
        super.created()
        providerId = runCatching { pageData.params.optString("providerId") }.getOrDefault("")
        KLog.i(UI_LOG, "🖼 [Render] $TAG 页面创建 providerId=$providerId")
        if (providerId.isNotEmpty()) loadDetail()
    }

    private fun loadDetail() {
        bridge().getProviderDetail(providerId) { json ->
            safeRun("$TAG getProviderDetail") {
                val j = json ?: run {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 回调为空")
                    return@safeRun
                }
                if (j.optString("error").isNotEmpty()) {
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 拉取失败: ${j.optString("error")}")
                    return@safeRun
                }
                providerName = j.optString("name")
                providerType = j.optString("type")
                enabled = j.optBoolean("enabled")
                nameInput = providerName
                baseUrlInput = j.optString("baseUrl")
                apiKeyInput = j.optString("apiKey")
                models.clear()
                j.optJSONArray("models")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { o ->
                            models.add(
                                ProviderModelItem(
                                    modelId = o.optString("modelId"),
                                    displayName = o.optString("displayName"),
                                    type = o.optString("type")
                                )
                            )
                        }
                    }
                }
                KLog.i(UI_LOG, "📦 [Data] $TAG 详情加载 [$providerName] models=${models.size} key=${j.optString("apiKeyMasked")}")
            }
        }
    }

    /** 保存编辑 */
    private fun save() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 保存 [$nameInput]")
        bridge().updateProvider(providerId, nameInput, baseUrlInput, apiKeyInput) { json ->
            safeRun("$TAG updateProvider") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    providerName = nameInput
                    ctxToast("保存成功")
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 保存成功 [$providerName]")
                } else {
                    ctxToast("保存失败")
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 保存失败")
                }
            }
        }
    }

    /** 切换启用 */
    private fun toggleEnabled(value: Boolean) {
        bridge().setProviderEnabled(providerId, value) { json ->
            safeRun("$TAG setProviderEnabled") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    enabled = value
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 启用 → $value")
                }
            }
        }
    }

    /** 测试连通性（原生 listModels） */
    private fun testConnection() {
        if (testing) return
        testing = true
        testResult = "测试中..."
        KLog.i(UI_LOG, "👆 [Click] $TAG 测试连通性 [$providerName]")
        bridge().testProvider(providerId) { json ->
            safeRun("$TAG testProvider") {
                val j = json ?: return@safeRun
                testing = false
                if (j.optBoolean("ok")) {
                    testResult = "✅ 连通成功，拉到 ${j.optInt("modelCount")} 个模型"
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 测试成功 ${j.optInt("modelCount")} 个模型")
                } else {
                    testResult = "❌ 失败: ${j.optString("error").take(60)}"
                    KLog.e(UI_LOG, "⚠ [Error] $TAG 测试失败: ${j.optString("error")}")
                }
            }
        }
    }

    /** 删除供应商 */
    private fun delete() {
        KLog.i(UI_LOG, "👆 [Click] $TAG 删除 [$providerName]")
        bridge().deleteProvider(providerId) { json ->
            safeRun("$TAG deleteProvider") {
                val j = json ?: return@safeRun
                if (j.optBoolean("ok")) {
                    KLog.i(UI_LOG, "✅ [Bridge] $TAG 已删除，返回列表")
                    closePage()
                }
            }
        }
    }

    private fun ctxToast(msg: String) = bridge().toast(msg)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = if (ctx.providerName.isNotEmpty()) ctx.providerName else "供应商详情",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }

                    // ===== 启用开关 =====
                    View {
                        attr {
                            marginLeft(16f)
                            marginRight(16f)
                            marginBottom(12f)
                            borderRadius(12f)
                            backgroundColor(HelixTheme.current.surfaceContainerLow)
                        }
                        SwitchRow(
                            title = "启用供应商",
                            subtitle = "禁用后不会出现在模型选择列表",
                            checked = { ctx.enabled },
                            onCheckedChange = { ctx.toggleEnabled(it) }
                        )
                    }

                    // ===== 基础配置（只读展示，编辑功能二期） =====
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "供应商名称",
                                subtitle = ctx.nameInput.ifEmpty { "未设置" },
                                icon = "🏷️"
                            ),
                            CardGroupItem(
                                title = "API Base URL",
                                subtitle = ctx.baseUrlInput.ifEmpty { "未设置" },
                                icon = "🔗"
                            ),
                            CardGroupItem(
                                title = "API Key",
                                subtitle = if (ctx.apiKeyInput.isEmpty()) "未配置" else "已配置（编辑功能二期）",
                                icon = "🔑"
                            ),
                            CardGroupItem(
                                title = "类型",
                                subtitle = ctx.providerType,
                                icon = "📦"
                            )
                        )
                    )

                    // ===== 连通性测试 =====
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "测试连通性",
                                subtitle = if (ctx.testResult.isEmpty()) "从供应商拉取模型列表验证配置" else ctx.testResult,
                                icon = "📡",
                                onClick = { ctx.testConnection() }
                            )
                        )
                    )

                    // ===== 模型列表（只读） =====
                    vif({ ctx.models.isNotEmpty() }) {
                        View {
                            attr {
                                marginLeft(16f)
                                marginRight(16f)
                                marginTop(8f)
                                marginBottom(8f)
                            }
                            Text {
                                attr {
                                    text("模型列表（${ctx.models.size}）")
                                    fontSize(13f)
                                    fontWeight700()
                                    color(HelixTheme.current.onSurface)
                                    marginBottom(4f)
                                }
                            }
                            vfor({ ctx.models }) { m ->
                                View {
                                    attr {
                                        flexDirectionColumn()
                                        padding(10f)
                                        marginBottom(1f)
                                        borderRadius(8f)
                                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                                    }
                                    Text {
                                        attr {
                                            text(m.displayName.ifEmpty { m.modelId })
                                            fontSize(13f)
                                            color(HelixTheme.current.onSurface)
                                        }
                                    }
                                    Text {
                                        attr {
                                            text("${m.modelId} · ${m.type}")
                                            fontSize(10f)
                                            fontFamily("monospace")
                                            color(HelixTheme.current.onSurfaceVariant)
                                            marginTop(2f)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ===== 删除 =====
                    View {
                        attr {
                            height(44f)
                            borderRadius(22f)
                            justifyContentCenter()
                            alignItemsCenter()
                            marginLeft(16f)
                            marginRight(16f)
                            marginTop(8f)
                            marginBottom(32f)
                            backgroundColor(HelixTheme.current.errorContainer)
                        }
                        event {
                            click {
                                safeRun("$TAG 删除按钮") { ctx.delete() }
                            }
                        }
                        Text {
                            attr {
                                text("删除供应商")
                                fontSize(15f)
                                color(HelixTheme.current.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}
