package com.helix.kuikly.pages.workspace

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.components.ui.NavBar
import com.helix.kuikly.pager.HelixBasePage
import com.helix.kuikly.theme.HelixTheme

/**
 * 文件编辑器页面（对应 Compose 的 WorkspaceFileEditorPage）
 *
 * 代码/文本编辑器：编辑区 + 保存操作
 */
@Page("WorkspaceFileEditorPage")
internal class WorkspaceFileEditorPage : HelixBasePage() {

    override fun body(): ViewBuilder {
        val page = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(HelixTheme.current.background)
                }
                NavBar(
                    title = "文件编辑",
                    showBack = true,
                    onBack = { page.closePage() }
                )
                // 编辑区（占位：真实编辑器通过 Module 桥接 CodeEditor）
                View {
                    attr {
                        flex(1f)
                        backgroundColor(HelixTheme.Dark.background)
                        margin(8f)
                        borderRadius(12f)
                        padding(12f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text("⌨️")
                            fontSize(48f)
                        }
                    }
                    Text {
                        attr {
                            text("代码编辑器由原生层渲染\n（通过 EditorModule 桥接）")
                            fontSize(13f)
                            color(HelixTheme.Dark.onSurface)
                            marginTop(8f)
                        }
                    }
                }
            }
        }
    }
}
