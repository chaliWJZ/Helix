package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 搜索服务详情页面（对应 Compose 的 SettingSearchDetailPage，原1027行）
 *
 * 搜索引擎配置：API密钥/参数/测试
 */
@Page("SettingSearchDetailPage")
internal class SettingSearchDetailPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "搜索服务详情",
                    showBack = true,
                    onBack = { page.closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "API 密钥",
                                subtitle = "已配置（****）",
                                icon = "🔑",
                                onClick = { page.bridge().toast("编辑 API Key（待接入）") }
                            ),
                            CardGroupItem(
                                title = "搜索结果数量",
                                subtitle = "每次搜索返回 10 条",
                                icon = "🔢",
                                onClick = { page.bridge().toast("设置结果数量（待接入）") }
                            ),
                            CardGroupItem(
                                title = "安全搜索",
                                subtitle = "过滤成人内容",
                                icon = "🛡️",
                                onClick = { page.bridge().toast("安全搜索设置（待接入）") }
                            ),
                            CardGroupItem(
                                title = "测试搜索",
                                subtitle = "发送测试查询验证配置",
                                icon = "🔍",
                                onClick = { page.bridge().toast("正在测试...（待接入）") }
                            )
                        )
                    )
                }
            }
        }
    }
}
