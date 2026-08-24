package com.helix.kuikly.pages.extensions

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.theme.HelixTheme

/**
 * 技能页面（对应 Compose 的 SkillsPage）
 *
 * Agent 技能列表管理
 */
@Page("SkillsPage")
internal class SkillsPage : Pager() {

    private fun router(): RouterModule = acquireModule(RouterModule.MODULE_NAME)

    data class Skill(
        val name: String,
        val description: String,
        val enabled: Boolean
    )

    private val skills by observableList<Skill>()

    override fun created() {
        super.created()
        listOf(
            Skill("web_search", "联网搜索最新信息", true),
            Skill("code_interpreter", "运行代码片段并返回结果", false),
            Skill("image_gen", "生成图片", true),
            Skill("file_read", "读取本地文件内容", false),
            Skill("calculator", "精确数学计算", true)
        ).forEach { skills.add(it) }
    }

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.Light.background)
                }
                NavBar(
                    title = "技能",
                    showBack = true,
                    onBack = { page.router().closePage() }
                )
                View {
                    attr {
                        flex(1f)
                        marginTop(8f)
                    }
                    vfor({ page.skills }) { item ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                padding(16f)
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(8f)
                                borderRadius(16f)
                                backgroundColor(HelixTheme.Light.surfaceContainerLow)
                            }
                            View {
                                attr {
                                    flex(1f)
                                }
                                Text {
                                    attr {
                                        text(item.name)
                                        fontSize(15f)
                                        color(HelixTheme.Light.onSurface)
                                    }
                                }
                                Text {
                                    attr {
                                        text(item.description)
                                        fontSize(13f)
                                        color(HelixTheme.Light.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            // 状态标签
                            Text {
                                attr {
                                    text(if (item.enabled) "已启用" else "未启用")
                                    fontSize(12f)
                                    color(
                                        if (item.enabled) HelixTheme.Light.primary
                                        else HelixTheme.Light.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
