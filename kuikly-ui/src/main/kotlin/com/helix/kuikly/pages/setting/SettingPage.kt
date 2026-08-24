package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SectionHeader
import com.helix.kuikly.theme.HelixTheme

/**
 * 主设置页面（对应 Compose 的 SettingPage）
 * 
 * 4 个分组：通用设置 / 模型服务 / 数据设置 / 关于
 */
@Page("SettingPage")
internal class SettingPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    // 颜色模式状态
    private var colorMode by observable(0) // 0=系统 1=亮色 2=暗色

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "设置",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }

                    // ==================== 通用设置 ====================
                    SectionHeader(title = "通用设置")
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "颜色模式",
                                subtitle = when (page.colorMode) {
                                    1 -> "亮色"
                                    2 -> "暗色"
                                    else -> "跟随系统"
                                },
                                icon = "☀️",
                                onClick = {
                                    // 切换颜色模式：系统→亮色→暗色→系统
                                    page.colorMode = (page.colorMode + 1) % 3
                                }
                            ),
                            CardGroupItem(
                                title = "偏好设置",
                                subtitle = "应用偏好和个性化配置",
                                icon = "⚙️",
                                onClick = { page.router().openPage("SettingPreferencesPage") }
                            ),
                            CardGroupItem(
                                title = "助手",
                                subtitle = "管理 AI 助手角色",
                                icon = "👤",
                                onClick = { page.router().openPage("AssistantPage") }
                            ),
                            CardGroupItem(
                                title = "扩展",
                                subtitle = "安装和管理扩展功能",
                                icon = "📦",
                                onClick = { page.router().openPage("ExtensionsPage") }
                            )
                        )
                    )

                    // ==================== 模型服务 ====================
                    SectionHeader(title = "模型与服务")
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "默认模型",
                                subtitle = "设置聊天/翻译/标题生成模型",
                                icon = "✨",
                                onClick = { page.router().openPage("SettingModelPage") }
                            ),
                            CardGroupItem(
                                title = "供应商",
                                subtitle = "管理 AI 服务供应商配置",
                                icon = "🧠",
                                onClick = { page.router().openPage("SettingProviderPage") }
                            ),
                            CardGroupItem(
                                title = "搜索服务",
                                subtitle = "管理互联网搜索引擎",
                                icon = "🔍",
                                onClick = { page.router().openPage("SettingSearchPage") }
                            ),
                            CardGroupItem(
                                title = "TTS 服务",
                                subtitle = "配置文字转语音引擎",
                                icon = "📢",
                                onClick = { page.router().openPage("SettingSpeechPage") }
                            ),
                            CardGroupItem(
                                title = "MCP",
                                subtitle = "管理 Model Context Protocol 服务器",
                                icon = "🔗",
                                onClick = { page.router().openPage("SettingMcpPage") }
                            ),
                            CardGroupItem(
                                title = "Web 服务器",
                                subtitle = "在浏览器中访问 Helix",
                                icon = "🖥️",
                                onClick = { page.router().openPage("SettingWebPage") }
                            )
                        )
                    )

                    // ==================== 数据设置 ====================
                    SectionHeader(title = "数据")
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "数据备份",
                                subtitle = "备份和恢复应用数据",
                                icon = "💾",
                                onClick = { page.router().openPage("BackupPage") }
                            ),
                            CardGroupItem(
                                title = "聊天存储",
                                subtitle = "管理聊天文件和图片",
                                icon = "📁",
                                onClick = { page.router().openPage("SettingFilesPage") }
                            )
                        )
                    )

                    // ==================== 关于 ====================
                    SectionHeader(title = "关于")
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "关于",
                                subtitle = "版本信息和开源许可",
                                icon = "👏",
                                onClick = { page.router().openPage("SettingAboutPage") }
                            ),
                            CardGroupItem(
                                title = "分享",
                                subtitle = "分享 Helix 给朋友",
                                icon = "📤",
                                onClick = { /* TODO: 分享功能 */ }
                            ),
                            CardGroupItem(
                                title = "捐赠",
                                subtitle = "支持项目持续开发",
                                icon = "💖",
                                onClick = { page.router().openPage("SettingDonatePage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
