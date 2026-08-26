package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

// ==================== FormItem ====================

/**
 * 表单行（对应 Compose 的 FormItem）
 * 结构：[标签+描述+内容] [尾部控件(如Switch)]
 */
fun ViewContainer<*, *>.FormItem(
    label: String,
    description: String = "",
    content: String = "",
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            padding(16f)
        }
        View {
            attr {
                flex(1f)
                paddingRight(12f)
            }
            Text {
                attr {
                    text(label)
                    fontSize(16f)
                    color(HelixTheme.current.onSurface)
                }
            }
            if (description.isNotEmpty()) {
                Text {
                    attr {
                        text(description)
                        fontSize(12f)
                        color(HelixTheme.current.onSurfaceVariant)
                        marginTop(2f)
                    }
                }
            }
            if (content.isNotEmpty()) {
                Text {
                    attr {
                        text(content)
                        fontSize(14f)
                        color(HelixTheme.current.onSurfaceVariant)
                        marginTop(4f)
                    }
                }
            }
        }
        // 尾部 Switch（如有）
        if (checked != null && onCheckedChange != null) {
            SwitchDot(checked, onCheckedChange)
        }
    }
}

// ==================== SwitchDot ====================

/**
 * 开关圆点（可复用的 Switch 核心部分）
 */
fun ViewContainer<*, *>.SwitchDot(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    View {
        attr {
            width(48f)
            height(28f)
            borderRadius(14f)
            backgroundColor(
                if (checked) HelixTheme.current.primary
                else HelixTheme.current.surfaceContainerHighest
            )
            justifyContentCenter()
        }
        event {
            click {
                com.tencent.kuikly.core.log.KLog.i(
                    "HelixKuikly",
                    "👆 [Click] 开关切换 → ${!checked}"
                )
                onCheckedChange(!checked)
            }
        }
        View {
            attr {
                width(22f)
                height(22f)
                borderRadius(11f)
                backgroundColor(Color(0xFFFFFFFFL))
                marginLeft(if (checked) 22f else 2f)
            }
        }
    }
}

// ==================== Select ====================

/**
 * 下拉选择器（对应 Compose 的 Select + ExposedDropdownMenuBox）
 * 
 * Kuikly 中无直接等价组件，用条件渲染的选项列表实现
 */
fun ViewContainer<*, *>.Select(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    View {
        attr {
            marginTop(8f)
            marginBottom(8f)
        }
        if (label.isNotEmpty()) {
            Text {
                attr {
                    text(label)
                    fontSize(14f)
                    color(HelixTheme.current.onSurfaceVariant)
                    marginBottom(4f)
                }
            }
        }
        // 当前选中项（点击展开选项）
        View {
            attr {
                height(48f)
                borderRadius(8f)
                paddingLeft(16f)
                paddingRight(16f)
                flexDirectionRow()
                alignItemsCenter()
                backgroundColor(HelixTheme.current.surfaceContainer)
            }
            event {
                click { KLog.i("HelixKuikly", "👆 [Click] 下拉[$label]"); onSelect(-1) } // -1 = toggle dropdown
            }
            Text {
                attr {
                    text(if (selectedIndex in options.indices) options[selectedIndex] else "请选择")
                    fontSize(16f)
                    color(HelixTheme.current.onSurface)
                    flex(1f)
                }
            }
            Text {
                attr {
                    text("▾")
                    fontSize(14f)
                    color(HelixTheme.current.onSurfaceVariant)
                }
            }
        }
    }
}

// ==================== SelectDropdown ====================

/**
 * 下拉选项列表（配合 Select 使用）
 * 展开时显示全部选项，点击选中后回调
 */
fun ViewContainer<*, *>.SelectDropdown(
    visible: Boolean,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        View {
            attr {
                positionAbsolute()
                top(0f)
                left(0f)
                right(0f)
                bottom(0f)
                backgroundColor(Color(0x40000000L))
            }
            event {
                click { onDismiss() }
            }
            View {
                attr {
                    marginTop(100f)
                    marginLeft(16f)
                    marginRight(16f)
                    borderRadius(12f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                }
                options.forEachIndexed { index, option ->
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            padding(14f)
                            paddingLeft(16f)
                        }
                        event {
                            click {
                                KLog.i("HelixKuikly", "👆 [Click] 下拉选项[$option]")
                                onSelect(index)
                                onDismiss()
                            }
                        }
                        Text {
                            attr {
                                text(option)
                                fontSize(16f)
                                color(
                                    if (index == selectedIndex) HelixTheme.current.primary
                                    else HelixTheme.current.onSurface
                                )
                                flex(1f)
                            }
                        }
                        if (index == selectedIndex) {
                            Text {
                                attr {
                                    text("✓")
                                    fontSize(16f)
                                    color(HelixTheme.current.primary)
                                }
                            }
                        }
                    }
                    // 分割线
                    if (index < options.size - 1) {
                        View {
                            attr {
                                height(0.5f)
                                marginLeft(16f)
                                marginRight(16f)
                                backgroundColor(HelixTheme.current.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TextArea ====================

/**
 * 多行文本输入（对应 Compose 的 TextArea）
 */
fun ViewContainer<*, *>.TextArea(
    label: String,
    value: String,
    placeholder: String = "",
    minHeight: Float = 100f
) {
    View {
        attr {
            marginTop(8f)
            marginBottom(8f)
        }
        if (label.isNotEmpty()) {
            Text {
                attr {
                    text(label)
                    fontSize(14f)
                    color(HelixTheme.current.onSurfaceVariant)
                    marginBottom(4f)
                }
            }
        }
        View {
            attr {
                minHeight(minHeight)
                borderRadius(8f)
                paddingLeft(12f)
                paddingRight(12f)
                paddingTop(10f)
                paddingBottom(10f)
                backgroundColor(HelixTheme.current.surfaceContainer)
            }
            Text {
                attr {
                    text(if (value.isEmpty()) placeholder else value)
                    fontSize(16f)
                    color(if (value.isEmpty()) HelixTheme.current.outline else HelixTheme.current.onSurface)
                }
            }
        }
    }
}

// ==================== StickyHeader ====================

/**
 * 吸顶头部（对应 Compose 的 StickyHeader）
 * 在列表滚动时固定在顶部
 */
fun ViewContainer<*, *>.StickyHeader(
    title: String,
    trailing: String = ""
) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(16f)
            paddingRight(16f)
            paddingTop(8f)
            paddingBottom(8f)
            backgroundColor(HelixTheme.current.background)
        }
        Text {
            attr {
                text(title)
                fontSize(13f)
                color(HelixTheme.current.primary)
                flex(1f)
            }
        }
        if (trailing.isNotEmpty()) {
            Text {
                attr {
                    text(trailing)
                    fontSize(13f)
                    color(HelixTheme.current.onSurfaceVariant)
                }
            }
        }
    }
}

// ==================== ShareSheet ====================

/**
 * 分享面板（对应 Compose 的 ShareSheet）
 * 底部弹出的分享选项
 */
fun ViewContainer<*, *>.ShareSheet(
    visible: Boolean,
    title: String = "分享",
    options: List<ShareOption> = listOf(),
    onDismiss: () -> Unit = {}
) {
    if (visible) {
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
                click { onDismiss() }
            }
            View {
                attr {
                    borderRadius(20f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                    paddingBottom(20f)
                }
                // 拖拽指示条
                View {
                    attr {
                        width(36f)
                        height(4f)
                        borderRadius(2f)
                        backgroundColor(HelixTheme.current.outlineVariant)
                        marginTop(8f)
                        marginBottom(16f)
                        alignSelfCenter()
                    }
                }
                // 标题
                Text {
                    attr {
                        text(title)
                        fontSize(16f)
                        color(HelixTheme.current.onSurface)
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }
                // 选项列表
                options.forEach { option ->
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            padding(14f)
                            paddingLeft(16f)
                        }
                        event {
                            click {
                                option.onClick()
                                onDismiss()
                            }
                        }
                        Text {
                            attr {
                                text(option.icon)
                                fontSize(20f)
                                marginRight(12f)
                            }
                        }
                        Text {
                            attr {
                                text(option.label)
                                fontSize(16f)
                                color(HelixTheme.current.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ShareOption(
    val icon: String,
    val label: String,
    val onClick: () -> Unit
)

// ==================== UIAvatar ====================

/**
 * 用户头像（对应 Compose 的 UIAvatar）
 * 圆形头像，带首字母fallback
 */
fun ViewContainer<*, *>.UIAvatar(
    name: String,
    size: Float = 40f,
    backgroundColor: Color = HelixTheme.current.primaryContainer
) {
    View {
        attr {
            width(size)
            height(size)
            borderRadius(size / 2)
            backgroundColor(backgroundColor)
            justifyContentCenter()
            alignItemsCenter()
        }
        Text {
            attr {
                text(name.take(1).uppercase())
                fontSize(size * 0.4f)
                color(HelixTheme.current.onPrimaryContainer)
            }
        }
    }
}

// ==================== SectionHeader ====================

/**
 * 分区标题（大标题 + 可选的尾部操作）
 */
fun ViewContainer<*, *>.SectionHeader(
    title: String,
    trailing: String = "",
    onTrailingClick: (() -> Unit)? = null
) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(16f)
            paddingRight(16f)
            paddingTop(16f)
            paddingBottom(8f)
        }
        Text {
            attr {
                text(title)
                fontSize(22f)
                color(HelixTheme.current.onBackground)
                flex(1f)
            }
        }
        if (trailing.isNotEmpty()) {
            View {
                attr {
                    paddingLeft(8f)
                    paddingRight(8f)
                }
                event {
                    onTrailingClick?.let { click { it() } }
                }
                Text {
                    attr {
                        text(trailing)
                        fontSize(14f)
                        color(HelixTheme.current.primary)
                    }
                }
            }
        }
    }
}

// ==================== EmptyView ====================

/**
 * 空状态视图
 */
fun ViewContainer<*, *>.EmptyView(
    message: String = "暂无数据",
    icon: String = "📭"
) {
    View {
        attr {
            justifyContentCenter()
            alignItemsCenter()
            padding(48f)
        }
        Text {
            attr {
                text(icon)
                fontSize(48f)
                marginBottom(12f)
            }
        }
        Text {
            attr {
                text(message)
                fontSize(14f)
                color(HelixTheme.current.onSurfaceVariant)
            }
        }
    }
}
