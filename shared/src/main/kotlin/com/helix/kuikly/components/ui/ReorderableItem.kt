package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 可拖拽排序列表项容器（对齐 Compose 的 ReorderableItem + longPressDraggableHandle）
 *
 * 工作方式（Kuikly 无原生 Reorderable 库，pan 手势状态机自实现）：
 * - 右侧拖动手柄（⋮⋮），pan state==start → 触觉(50ms) + 进入拖动模式（回调 onDragStart）
 * - state==move → 回调 onDragMove(pageY)（页面层显示浮动预览跟随手指）
 * - state==end → 回调 onDragEnd()（页面层计算目标行 → 桥落位 → 触觉(30ms)）
 * - 拖动中此项变半透明占位（opacity 0.3），其余视觉由页面层控制
 *
 * 用法：页面维护 dragIndex(-1=非拖动)/dragLabel/dragPageY 三个 observable，
 * onDragEnd 里 delta 行数 = (dragPageY - startPageY) / rowHeight，调 move 桥。
 */
fun ViewContainer<*, *>.ReorderableItem(
    isDragging: Boolean,
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
        }
        // 主内容（拖动中半透明占位，对应 Compose scale(0.95) + 原位虚影）
        View {
            attr {
                flex(1f)
                opacity(if (isDragging) 0.3f else 1f)
            }
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
                    color(com.helix.kuikly.theme.HelixTheme.current.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 拖动浮动预览（页面根层调用，positionAbsolute 跟随手指）
 * 对齐 Compose 拖动时 item scale(0.95) 浮起效果
 */
fun ViewContainer<*, *>.DragPreview(
    visible: Boolean,
    label: String,
    pageY: Float
) {
    vif({ visible }) {
        View {
            attr {
                positionAbsolute()
                left(16f)
                right(16f)
                top(pageY - 50f)
                padding(12f)
                borderRadius(12f)
                backgroundColor(com.helix.kuikly.theme.HelixTheme.current.primary)
            }
            Text {
                attr {
                    text(label)
                    fontSize(15f)
                    fontWeight700()
                    color(com.helix.kuikly.theme.HelixTheme.current.onPrimary)
                }
            }
        }
    }
}
