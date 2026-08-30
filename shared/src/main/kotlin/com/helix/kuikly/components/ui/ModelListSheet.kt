package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

private const val TAG = "[ModelListSheet]"

/** 可选模型项 */
data class ModelOption(
    val id: String,
    val name: String,
    val provider: String
)

/**
 * 底部模型选择弹层（对应 Compose 的 ModelListSheet）
 *
 * - 遮罩点击关闭
 * - 按 provider 顺序列出全部 chat 模型
 * - 当前选中项高亮（✓）
 * - 顶部标题 + 当前槽位名
 */
fun ViewContainer<*, *>.ModelListSheet(
    visible: Boolean,
    title: String,
    models: List<ModelOption>,
    currentId: String,
    onSelect: (ModelOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        KLog.i(UI_LOG_TAG, "🖼 [Render] $TAG [$title] 可选模型=${models.size}")
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
                    safeRun("$TAG 遮罩关闭") {
                        KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 遮罩关闭")
                        onDismiss()
                    }
                }
            }
            // 底部面板
            View {
                attr {
                    alignSelfStretch()
                    height(460f)
                    borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                }
                event {
                    click {
                        safeRun("$TAG 面板阻止穿透") { /* 点击面板不关闭 */ }
                    }
                }
                // 标题行
                Text {
                    attr {
                        text(title)
                        fontSize(16f)
                        fontWeight700()
                        color(HelixTheme.current.onSurface)
                        marginLeft(16f)
                        marginTop(16f)
                        marginBottom(8f)
                    }
                }
                if (models.isEmpty()) {
                    Text {
                        attr {
                            text("暂无可用模型\n（请先在「供应商」页配置并启用供应商）")
                            fontSize(13f)
                            color(HelixTheme.current.onSurfaceVariant)
                            alignSelfCenter()
                            marginTop(64f)
                            textAlignCenter()
                        }
                    }
                }
                // 模型列表（静态 forEach：弹层每次打开重建，无需响应式）
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginBottom(16f)
                    }
                    models.forEach { m ->
                        View {
                            attr {
                                flexDirectionRow()
                                alignItemsCenter()
                                marginLeft(16f)
                                marginRight(16f)
                                marginBottom(1f)
                                padding(12f)
                                borderRadius(10f)
                                backgroundColor(
                                    if (m.id == currentId) HelixTheme.current.primaryContainer
                                    else HelixTheme.current.surfaceContainerLow
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG 选择模型 ${m.name}") {
                                        KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 选择 [${m.provider}] ${m.name}")
                                        onSelect(m)
                                    }
                                }
                            }
                            // 模型名 + provider
                            View {
                                attr { flex(1f) }
                                Text {
                                    attr {
                                        text(m.name)
                                        fontSize(14f)
                                        color(
                                            if (m.id == currentId) HelixTheme.current.onPrimaryContainer
                                            else HelixTheme.current.onSurface
                                        )
                                    }
                                }
                                Text {
                                    attr {
                                        text(m.provider)
                                        fontSize(11f)
                                        color(HelixTheme.current.onSurfaceVariant)
                                        marginTop(2f)
                                    }
                                }
                            }
                            // 选中标记
                            if (m.id == currentId) {
                                Text {
                                    attr {
                                        text("✓")
                                        fontSize(14f)
                                        fontWeight700()
                                        color(HelixTheme.current.primary)
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
}
