package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

private const val TAG = "[Sheet]"

/**
 * 单字段编辑弹层（底部，遮罩点击关闭）
 *
 * Input 采用「行布局容器 + flex(1f)」包裹（已验证可渲染模式，规避列布局 Input 不显示的坑）
 */
fun ViewContainer<*, *>.EditSheet(
    visible: () -> Boolean,
    title: String,
    label: String,
    initialValue: String,
    placeholder: String = "",
    maskDisplay: Boolean = false,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible()) {
        KLog.i(UI_LOG_TAG, "🖼 [Render] $TAG EditSheet [$label]")
        var text = initialValue
        View {
            attr {
                positionAbsolute()
                top(0f); left(0f); right(0f); bottom(0f)
                backgroundColor(Color(0x80000000L))
                justifyContentFlexEnd()
            }
            event {
                click {
                    safeRun("$TAG 编辑弹层遮罩关闭") {
                        KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 编辑弹层[$label]遮罩关闭")
                        onDismiss()
                    }
                }
            }
            View {
                attr {
                    alignSelfStretch()
                    padding(16f)
                    borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                }
                event {
                    click { safeRun("$TAG 面板阻止穿透") { /* 阻止穿透 */ } }
                }
                Text {
                    attr {
                        text(title)
                        fontSize(16f)
                        fontWeight700()
                        color(HelixTheme.current.onSurface)
                        marginBottom(12f)
                    }
                }
                Text {
                    attr {
                        text(label)
                        fontSize(13f)
                        color(HelixTheme.current.onSurfaceVariant)
                        marginBottom(6f)
                    }
                }
                // Input 行容器（已验证模式）
                View {
                    attr {
                        height(48f)
                        borderRadius(10f)
                        backgroundColor(HelixTheme.current.surfaceContainerLow)
                        flexDirectionRow()
                        alignItemsCenter()
                        paddingLeft(12f)
                        paddingRight(12f)
                    }
                    Input {
                        attr {
                            text(if (maskDisplay) "" else initialValue)
                            placeholder(placeholder)
                            fontSize(15f)
                            flex(1f)
                        }
                        event {
                            textDidChange { text = it.text }
                        }
                    }
                    if (maskDisplay) {
                        Text {
                            attr {
                                text("●●●●●●（点击重新输入）")
                                fontSize(13f)
                                color(HelixTheme.current.onSurfaceVariant)
                            }
                        }
                    }
                }
                // 按钮行
                View {
                    attr {
                        flexDirectionRow()
                        marginTop(16f)
                        marginBottom(16f)
                    }
                    View {
                        attr {
                            height(44f)
                            flex(1f)
                            borderRadius(22f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(HelixTheme.current.surfaceContainer)
                            marginRight(8f)
                        }
                        event {
                            click {
                                safeRun("$TAG 编辑取消") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 编辑弹层[$label]取消")
                                    onDismiss()
                                }
                            }
                        }
                        Text {
                            attr {
                                text("取消")
                                fontSize(15f)
                                color(HelixTheme.current.onSurface)
                            }
                        }
                    }
                    View {
                        attr {
                            height(44f)
                            flex(1f)
                            borderRadius(22f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(HelixTheme.current.primary)
                            marginLeft(8f)
                        }
                        event {
                            click {
                                safeRun("$TAG 编辑保存") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 编辑弹层[$label]保存 → ${text.take(24)}")
                                    onSave(text)
                                }
                            }
                        }
                        Text {
                            attr {
                                text("保存")
                                fontSize(15f)
                                color(HelixTheme.current.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 选项数据 */
data class SheetOption(val value: String, val label: String, val subtitle: String = "")

/**
 * 选项选择弹层（类型切换/TTL/模型类型等单选场景）
 */
fun ViewContainer<*, *>.OptionSheet(
    visible: () -> Boolean,
    title: String,
    options: List<SheetOption>,
    currentValue: String,
    onSelect: (SheetOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible()) {
        KLog.i(UI_LOG_TAG, "🖼 [Render] $TAG OptionSheet [$title] options=${options.size}")
        View {
            attr {
                positionAbsolute()
                top(0f); left(0f); right(0f); bottom(0f)
                backgroundColor(Color(0x80000000L))
                justifyContentFlexEnd()
            }
            event {
                click {
                    safeRun("$TAG 选项弹层遮罩关闭") {
                        KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 选项弹层[$title]遮罩关闭")
                        onDismiss()
                    }
                }
            }
            View {
                attr {
                    alignSelfStretch()
                    height(400f)
                    borderRadius(topLeft = 16f, topRight = 16f, bottomLeft = 0f, bottomRight = 0f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                }
                event {
                    click { safeRun("$TAG 面板阻止穿透") { /* 阻止穿透 */ } }
                }
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
                Scroller {
                    attr {
                        flex(1f)
                        alignSelfStretch()
                        marginBottom(16f)
                    }
                    options.forEach { o ->
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
                                    if (o.value == currentValue) HelixTheme.current.primaryContainer
                                    else HelixTheme.current.surfaceContainerLow
                                )
                            }
                            event {
                                click {
                                    safeRun("$TAG 选择${o.label}") {
                                        KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 选项弹层[$title]选择 ${o.label}")
                                        onSelect(o)
                                    }
                                }
                            }
                            View {
                                attr { flex(1f) }
                                Text {
                                    attr {
                                        text(o.label)
                                        fontSize(14f)
                                        color(
                                            if (o.value == currentValue) HelixTheme.current.onPrimaryContainer
                                            else HelixTheme.current.onSurface
                                        )
                                    }
                                }
                                if (o.subtitle.isNotEmpty()) {
                                    Text {
                                        attr {
                                            text(o.subtitle)
                                            fontSize(11f)
                                            color(HelixTheme.current.onSurfaceVariant)
                                            marginTop(2f)
                                        }
                                    }
                                }
                            }
                            if (o.value == currentValue) {
                                Text {
                                    attr {
                                        text("✓")
                                        fontSize(14f)
                                        color(HelixTheme.current.primary)
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

/**
 * 确认弹层（删除等危险操作二次确认）
 */
fun ViewContainer<*, *>.ConfirmSheet(
    visible: () -> Boolean,
    title: String,
    message: String,
    confirmText: String = "确认",
    danger: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible()) {
        KLog.i(UI_LOG_TAG, "🖼 [Render] $TAG ConfirmSheet [$title]")
        View {
            attr {
                positionAbsolute()
                top(0f); left(0f); right(0f); bottom(0f)
                backgroundColor(Color(0x80000000L))
                justifyContentCenter()
                alignItemsCenter()
            }
            event {
                click {
                    safeRun("$TAG 确认弹层遮罩关闭") { onDismiss() }
                }
            }
            View {
                attr {
                    width(300f)
                    borderRadius(16f)
                    padding(20f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                }
                event {
                    click { safeRun("$TAG 卡片阻止穿透") { /* 阻止穿透 */ } }
                }
                Text {
                    attr {
                        text(title)
                        fontSize(16f)
                        fontWeight700()
                        color(HelixTheme.current.onSurface)
                        marginBottom(10f)
                    }
                }
                Text {
                    attr {
                        text(message)
                        fontSize(13f)
                        color(HelixTheme.current.onSurfaceVariant)
                        marginBottom(18f)
                    }
                }
                View {
                    attr { flexDirectionRow() }
                    View {
                        attr {
                            height(40f)
                            flex(1f)
                            borderRadius(20f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(HelixTheme.current.surfaceContainer)
                            marginRight(6f)
                        }
                        event {
                            click {
                                safeRun("$TAG 确认弹层取消") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 确认弹层[$title]取消")
                                    onDismiss()
                                }
                            }
                        }
                        Text {
                            attr {
                                text("取消")
                                fontSize(14f)
                                color(HelixTheme.current.onSurface)
                            }
                        }
                    }
                    View {
                        attr {
                            height(40f)
                            flex(1f)
                            borderRadius(20f)
                            justifyContentCenter()
                            alignItemsCenter()
                            backgroundColor(if (danger) HelixTheme.current.error else HelixTheme.current.primary)
                            marginLeft(6f)
                        }
                        event {
                            click {
                                safeRun("$TAG 确认弹层确认") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $TAG 确认弹层[$title]确认")
                                    onConfirm()
                                }
                            }
                        }
                        Text {
                            attr {
                                text(confirmText)
                                fontSize(14f)
                                color(if (danger) Color(0xFFFFFFFFL) else HelixTheme.current.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
