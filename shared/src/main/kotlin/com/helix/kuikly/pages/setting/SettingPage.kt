package com.helix.kuikly.pages.setting

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Scroller
import com.helix.kuikly.components.ui.CardGroup
import com.helix.kuikly.components.ui.CardGroupItem
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.components.ui.SectionHeader
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme
import com.tencent.kuikly.core.views.View

/**
 * 主设置页面（对应 Compose 的 SettingPage）
 * 
 * 4 个分组：通用设置 / 模型服务 / 数据设置 / 关于
 */
@Page("SettingPage")
internal class SettingPage : HelixBasePage() {

    // 颜色模式状态

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "设置",
                    showBack = true,
                    onBack = { ctx.closePage() }
                )
                // 内容区域（Scroller 支持滚动）
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginTop(8f)
                    }

                    // ==================== 通用设置 ====================
                    SectionHeader(title = "通用设置")
                    CardGroup(
                        items = listOf(
                            CardGroupItem(
                                title = "偏好设置",
                                subtitle = "应用偏好和个性化配置",
                                icon = "⚙️",
                                onClick = { ctx.openPage("SettingPreferencesPage") }
                            ),
                            CardGroupItem(
                                title = "助手",
                                subtitle = "管理 AI 助手角色",
                                icon = "👤",
                                onClick = { ctx.openPage("AssistantPage") }
                            ),
                            CardGroupItem(
                                title = "扩展",
                                subtitle = "安装和管理扩展功能",
                                icon = "📦",
                                onClick = { ctx.openPage("ExtensionsPage") }
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
                                onClick = { ctx.openPage("SettingModelPage") }
                            ),
                            CardGroupItem(
                                title = "供应商",
                                subtitle = "管理 AI 服务供应商配置",
                                icon = "🧠",
                                onClick = { ctx.openPage("SettingProviderPage") }
                            ),
                            CardGroupItem(
                                title = "搜索服务",
                                subtitle = "管理互联网搜索引擎",
                                icon = "🔍",
                                onClick = { ctx.openPage("SettingSearchPage") }
                            ),
                            CardGroupItem(
                                title = "TTS 服务",
                                subtitle = "配置文字转语音引擎",
                                icon = "📢",
                                onClick = { ctx.openPage("SettingSpeechPage") }
                            ),
                            CardGroupItem(
                                title = "MCP",
                                subtitle = "管理 Model Context Protocol 服务器",
                                icon = "🔗",
                                onClick = { ctx.openPage("SettingMcpPage") }
                            ),
                            CardGroupItem(
                                title = "Web 服务器",
                                subtitle = "在浏览器中访问 Helix",
                                icon = "🖥️",
                                onClick = { ctx.openPage("SettingWebPage") }
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
                                onClick = { ctx.openPage("BackupPage") }
                            ),
                            CardGroupItem(
                                title = "聊天存储",
                                subtitle = "管理聊天文件和图片",
                                icon = "📁",
                                onClick = { ctx.openPage("SettingFilesPage") }
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
                                onClick = { ctx.openPage("SettingAboutPage") }
                            ),
                            CardGroupItem(
                                title = "分享",
                                subtitle = "分享 Helix 给朋友",
                                icon = "📤",
                                // 修复 stbug B6：原为空 TODO 点击无反应，
                                // 改为调用原生分享（对齐 Compose 版 Intent 分享行为）
                                onClick = {
                                    ctx.bridge().shareText(
                                        "Helix - 一款开源 AI 助手客户端\nhttps://github.com/chaliWJZ/Helix"
                                    )
                                }
                            ),
                            CardGroupItem(
                                title = "捐赠",
                                subtitle = "支持项目持续开发",
                                icon = "💖",
                                onClick = { ctx.openPage("SettingDonatePage") }
                            )
                        )
                    )
                }
            }
        }
    }
}
