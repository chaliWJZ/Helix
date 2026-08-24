package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.helix.kuikly.theme.HelixTheme

// ==================== 数据类 ====================

data class NavBarAction(
    val label: String,
    val onClick: () -> Unit
)

data class CardGroupItem(
    val title: String,
    val subtitle: String = "",
    val icon: String = "",
    val onClick: (() -> Unit)? = null,
    val trailingText: String = ""
)

enum class TagType { DEFAULT, SUCCESS, ERROR, WARNING, INFO }

// ==================== NavBar ====================

/**
 * 顶部导航栏（对应 Compose 的 TopAppBar）
 */
fun ViewContainer<*, *>.NavBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    rightActions: List<NavBarAction> = listOf()
) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            height(56f)
            paddingLeft(8f)
            paddingRight(8f)
            backgroundColor(HelixTheme.Light.surface)
        }
        if (showBack) {
            View {
                attr {
                    width(40f)
                    height(40f)
                    justifyContentCenter()
                    alignItemsCenter()
                    borderRadius(20f)
                }
                event {
                    click { onBack() }
                }
                Text {
                    attr {
                        text("‹")
                        fontSize(24f)
                        color(HelixTheme.Light.onSurface)
                    }
                }
            }
        }
        Text {
            attr {
                text(title)
                fontSize(18f)
                color(HelixTheme.Light.onSurface)
                marginLeft(8f)
                flex(1f)
            }
        }
        rightActions.forEach { action ->
            View {
                attr {
                    paddingLeft(12f)
                    paddingRight(12f)
                    height(36f)
                    justifyContentCenter()
                    alignItemsCenter()
                    marginLeft(4f)
                }
                event {
                    click { action.onClick() }
                }
                Text {
                    attr {
                        text(action.label)
                        fontSize(14f)
                        color(HelixTheme.Light.primary)
                    }
                }
            }
        }
    }
}

// ==================== CardGroup ====================

fun ViewContainer<*, *>.CardGroup(
    items: List<CardGroupItem>,
    cornerRadius: Float = 20f
) {
    View {
        attr {
            borderRadius(cornerRadius)
            backgroundColor(HelixTheme.Light.surfaceContainerLow)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
        }
        items.forEachIndexed { index, item ->
            CardGroupRow(item, index < items.size - 1)
        }
    }
}

private fun ViewContainer<*, *>.CardGroupRow(item: CardGroupItem, showDivider: Boolean) {
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            padding(16f)
        }
        event {
            item.onClick?.let { handler ->
                click { handler() }
            }
        }
        if (item.icon.isNotEmpty()) {
            Text {
                attr {
                    text(item.icon)
                    fontSize(20f)
                    marginRight(12f)
                }
            }
        }
        View {
            attr {
                flex(1f)
            }
            Text {
                attr {
                    text(item.title)
                    fontSize(16f)
                    color(HelixTheme.Light.onSurface)
                }
            }
            if (item.subtitle.isNotEmpty()) {
                Text {
                    attr {
                        text(item.subtitle)
                        fontSize(14f)
                        color(HelixTheme.Light.onSurfaceVariant)
                        marginTop(2f)
                    }
                }
            }
        }
        if (item.trailingText.isNotEmpty()) {
            Text {
                attr {
                    text(item.trailingText)
                    fontSize(14f)
                    color(HelixTheme.Light.onSurfaceVariant)
                }
            }
        }
        if (item.onClick != null) {
            Text {
                attr {
                    text("›")
                    fontSize(18f)
                    color(HelixTheme.Light.onSurfaceVariant)
                    marginLeft(4f)
                }
            }
        }
    }
    if (showDivider) {
        View {
            attr {
                height(0.5f)
                marginLeft(16f)
                marginRight(16f)
                backgroundColor(HelixTheme.Light.outlineVariant)
            }
        }
    }
}

// ==================== TextInput ====================

fun ViewContainer<*, *>.TextInput(
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit = {}
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
                    color(HelixTheme.Light.onSurfaceVariant)
                    marginBottom(4f)
                }
            }
        }
        View {
            attr {
                height(48f)
                borderRadius(8f)
                paddingLeft(12f)
                paddingRight(12f)
                justifyContentCenter()
                backgroundColor(HelixTheme.Light.surfaceContainer)
            }
            Text {
                attr {
                    text(if (value.isEmpty()) placeholder else value)
                    fontSize(16f)
                    color(if (value.isEmpty()) HelixTheme.Light.outline else HelixTheme.Light.onSurface)
                }
            }
        }
    }
}

// ==================== Tag ====================

private fun tagBackgroundColor(type: TagType): Color {
    return when (type) {
        TagType.SUCCESS -> Color(0xFFCDF6D9L)
        TagType.ERROR -> Color(0xFFFDCDC9L)
        TagType.WARNING -> Color(0xFFFFE4BBL)
        TagType.INFO -> Color(0xFFD6E4FFL)
        else -> HelixTheme.Light.tertiaryContainer
    }
}

private fun tagTextColor(type: TagType): Color {
    return when (type) {
        TagType.SUCCESS -> Color(0xFF48A62EL)
        TagType.ERROR -> Color(0xFFCB2B2BL)
        TagType.WARNING -> Color(0xFFD8650CL)
        TagType.INFO -> Color(0xFF165DFFL)
        else -> HelixTheme.Light.onTertiaryContainer
    }
}

fun ViewContainer<*, *>.Tag(
    text: String,
    type: TagType = TagType.DEFAULT,
    onClick: (() -> Unit)? = null
) {
    View {
        attr {
            borderRadius(9999f)
            backgroundColor(tagBackgroundColor(type))
            paddingLeft(8f)
            paddingRight(8f)
            paddingTop(2f)
            paddingBottom(2f)
        }
        event {
            onClick?.let { handler ->
                click { handler() }
            }
        }
        Text {
            attr {
                text(text)
                fontSize(11f)
                color(tagTextColor(type))
            }
        }
    }
}

// ==================== SwitchRow ====================

fun ViewContainer<*, *>.SwitchRow(
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            }
            Text {
                attr {
                    text(title)
                    fontSize(16f)
                    color(HelixTheme.Light.onSurface)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text {
                    attr {
                        text(subtitle)
                        fontSize(14f)
                        color(HelixTheme.Light.onSurfaceVariant)
                        marginTop(2f)
                    }
                }
            }
        }
        View {
            attr {
                width(48f)
                height(28f)
                borderRadius(14f)
                backgroundColor(
                    if (checked) HelixTheme.Light.primary
                    else HelixTheme.Light.surfaceContainerHighest
                )
                justifyContentCenter()
            }
            event {
                click { onCheckedChange(!checked) }
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
}

// ==================== ConfirmDialog ====================

fun ViewContainer<*, *>.ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "确认",
    cancelText: String = "取消",
    onConfirm: () -> Unit = {},
    onCancel: () -> Unit = {}
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
                justifyContentCenter()
                alignItemsCenter()
            }
            event {
                click { onCancel() }
            }
            View {
                attr {
                    width(280f)
                    borderRadius(16f)
                    backgroundColor(HelixTheme.Light.surfaceContainerHigh)
                    padding(24f)
                }
                Text {
                    attr {
                        text(title)
                        fontSize(18f)
                        color(HelixTheme.Light.onSurface)
                        marginBottom(12f)
                    }
                }
                Text {
                    attr {
                        text(message)
                        fontSize(14f)
                        color(HelixTheme.Light.onSurfaceVariant)
                        marginBottom(24f)
                    }
                }
                View {
                    attr {
                        flexDirectionRow()
                        justifyContentFlexEnd()
                    }
                    View {
                        attr {
                            paddingLeft(16f)
                            paddingRight(16f)
                            paddingTop(8f)
                            paddingBottom(8f)
                        }
                        event {
                            click { onCancel() }
                        }
                        Text {
                            attr {
                                text(cancelText)
                                fontSize(14f)
                                color(HelixTheme.Light.primary)
                            }
                        }
                    }
                    View {
                        attr {
                            paddingLeft(16f)
                            paddingRight(16f)
                            paddingTop(8f)
                            paddingBottom(8f)
                        }
                        event {
                            click { onConfirm() }
                        }
                        Text {
                            attr {
                                text(confirmText)
                                fontSize(14f)
                                color(HelixTheme.Light.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== DotLoading ====================

fun ViewContainer<*, *>.DotLoading() {
    View {
        attr {
            justifyContentCenter()
            alignItemsCenter()
            padding(16f)
        }
        Text {
            attr {
                text("•  •  •")
                fontSize(16f)
                color(HelixTheme.Light.outline)
            }
        }
    }
}

// ==================== ErrorCard ====================

fun ViewContainer<*, *>.ErrorCard(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    View {
        attr {
            borderRadius(12f)
            backgroundColor(Color(0xFFFDF0EFL))
            padding(16f)
            marginLeft(16f)
            marginRight(16f)
            marginBottom(8f)
        }
        Text {
            attr {
                text("⚠ $message")
                fontSize(14f)
                color(Color(0xFFCB2B2BL))
            }
        }
        if (onRetry != null) {
            View {
                attr {
                    marginTop(8f)
                    paddingLeft(12f)
                    paddingRight(12f)
                    paddingTop(6f)
                    paddingBottom(6f)
                    borderRadius(6f)
                    backgroundColor(Color(0xFFCB2B2BL))
                }
                event {
                    click { onRetry() }
                }
                Text {
                    attr {
                        text("重试")
                        fontSize(12f)
                        color(Color(0xFFFFFFFFL))
                    }
                }
            }
        }
    }
}
