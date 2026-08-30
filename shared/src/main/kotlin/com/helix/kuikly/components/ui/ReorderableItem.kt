package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

/**
 * 可拖拽排序列表项容器（对齐 Compose ReorderableItem + longPressDraggableHandle）
 *
 * 官方响应式姿势（DevGuide/reactive-update.md）：
 * - isDragging 传 getter，attr 内调用 → dragIndex 变化时该项 opacity 自动更新
 * - 拖动中原位项 opacity 0（让出视觉空位），浮层由 DragPreview 承担
 */
fun ViewContainer<*, *>.ReorderableItem(
    isDragging: () -> Boolean,
    onDragStart: (pageY: Float) -> Unit,
    onDragMove: (pageY: Float) -> Unit,
    onDragEnd: () -> Unit,
    content: ViewContainer<*, *>.() -> Unit
) {
    val tag = componentTag("ReorderableItem")
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            // attr 内调用 getter 读响应式字段 → 自动更新（官方 reactive-update 机制）
            opacity(if (isDragging()) 0f else 1f)
        }
        // 卡片主体
        View {
            attr { flex(1f) }
            content()
        }
        // 拖动手柄（对齐 Compose IconButton + longPressDraggableHandle）
        View {
            attr {
                width(44f)
                height(44f)
                justifyContentCenter()
                alignItemsCenter()
            }
            event {
                pan { params ->
                    safeRun("$tag pan ${params.state}") {
                        when (params.state) {
                            "start" -> {
                                KLog.i(UI_LOG_TAG, "👆 [Drag-Start] $tag pageY=${params.pageY}")
                                onDragStart(params.pageY)
                            }
                            "move" -> onDragMove(params.pageY)
                            "end" -> {
                                KLog.i(UI_LOG_TAG, "👆 [Drag-End] $tag pageY=${params.pageY}")
                                onDragEnd()
                            }
                        }
                    }
                }
            }
            Text {
                attr {
                    text("⋮⋮")
                    fontSize(16f)
                    color(HelixTheme.current.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 拖动浮动预览（页面根层调用）——**实时跟手**。
 *
 * 官方响应式机制：pageY 传 getter，attr 内调用 → dragPageY 更新时 top 自动更新。
 * 对齐 Compose 拖动时整卡浮起跟随手指。
 */
fun ViewContainer<*, *>.DragPreview(
    visible: () -> Boolean,
    name: () -> String,
    enabled: () -> Boolean,
    modelCount: () -> Int,
    type: () -> String,
    pageY: () -> Float
) {
    vif({ visible() }) {
        View {
            attr {
                positionAbsolute()
                left(16f)
                right(16f)
                // attr 内调用 getter → 实时跟手（官方 reactive-update）
                top(pageY() - 60f)
                padding(12f)
                borderRadius(12f)
                backgroundColor(HelixTheme.current.primary)
            }
            View {
                attr {
                    flexDirectionRow()
                    alignItemsCenter()
                }
                Text {
                    attr {
                        text(if (type() == "openai") "🤖" else if (type() == "google") "🔵" else "🟠")
                        fontSize(22f)
                        marginRight(10f)
                    }
                }
                View {
                    attr { flex(1f) }
                    Text {
                        attr {
                            text(name())
                            fontSize(15f)
                            fontWeight700()
                            color(HelixTheme.current.onPrimary)
                        }
                    }
                    Text {
                        attr {
                            text(
                                (if (enabled()) "已启用" else "已禁用") +
                                    " · ${modelCount()} 个模型 · ${type()} · 拖动中…"
                            )
                            fontSize(11f)
                            color(HelixTheme.current.onPrimary)
                            marginTop(2f)
                        }
                    }
                }
            }
        }
    }
}
