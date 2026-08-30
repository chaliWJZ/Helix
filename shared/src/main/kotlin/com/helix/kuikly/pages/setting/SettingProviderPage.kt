package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.EmptyView
import com.helix.kuikly.components.ui.NavBar
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

/**
 * 供应商列表页面（对应 Compose 的 SettingProviderPage，真实数据）
 *
 * - pageDidAppear 每次出现刷新（详情页改动能同步回来）
 * - 列表项：名称 + 启用状态/模型数 + 类型
 * - 点击跳详情（传 providerId）
 */
@Page("SettingProviderPage")
internal class SettingProviderPage : HelixBasePage() {

    private val providers by observableList<ProviderItem>()

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
                KLog.i(UI_LOG, "📦 [Data] $TAG 加载 ${items.size} 个供应商")
            }
        }
    }

    private fun openDetail(item: ProviderItem) {
        KLog.i(UI_LOG, "👆 [Click] $TAG 打开供应商 [${item.name}] type=${item.type}")
        openPage(
            "SettingProviderDetailPage",
            JSONObject().apply { put("providerId", item.id) }
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
                    onBack = { ctx.closePage() }
                )
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }
                    vif({ ctx.providers.isEmpty() }) {
                        EmptyView(
                            message = "暂无供应商，在 Compose 版添加后同步显示",
                            icon = "🔌"
                        )
                    }
                    // 供应商列表（真实数据）
                    vfor({ ctx.providers }) { item ->
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
                                    marginLeft(8f)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
