package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.log.KLog
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

/** UI 交互日志统一 TAG（adb logcat -s HelixKuikly 可过滤） */
internal const val UI_LOG_TAG = "HelixKuikly"

/**
 * 构建期从调用栈自动提取「页面类名#方法名」。
 *
 * 必须在 View 树构建期间调用（body() 执行时调用栈里才有页面帧）。
 * 组件函数体顶部调用一次并持有，click 闭包捕获后点击时依然可用。
 */
internal inline fun callerTag(): String {
    val frame = Thread.currentThread().stackTrace.firstOrNull {
        it.className.startsWith("com.helix.kuikly.pages")
    } ?: return "[Unknown]"
    return "[${frame.className.substringAfterLast('.')}#${frame.methodName}]"
}

/**
 * 组件层统一日志前缀：「[页面类名#方法名] [组件名]」。
 *
 * 在组件函数体顶部调用（构建期求值），例：[SettingPreferencesPage#body] [SwitchRow]
 */
internal inline fun componentTag(component: String): String = "${callerTag()} [$component]"

/**
 * 安全执行（统一异常捕获 + 日志）
 *
 * 所有 click handler / 回调统一走此入口，防止异常导致页面崩溃或静默失败。
 * 用法：safeRun(tag, "描述") { 可能抛异常的代码 }
 */
internal inline fun safeRun(tag: String, block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        KLog.e(UI_LOG_TAG, "⚠ [Error] $tag: ${e.message}")
    }
}

// ==================== NavBar ====================

/**
 * 顶部导航栏（对应 Compose 的 TopAppBar）
 *
 * 布局：[返回]  [标题 flex(1) 省略号]  [操作1] [操作2] ...
 * 安全区：顶部 44px 让出状态栏，底部 16px 让出导航条
 */
fun ViewContainer<*, *>.NavBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    rightActions: List<NavBarAction> = listOf()
) {
    val tag = componentTag("NavBar")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [$title]")
    View {
        attr {
            flexDirectionColumn()
            backgroundColor(HelixTheme.current.surface)
        }
        // 状态栏占位
        View {
            attr {
                height(44f)
            }
        }
        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                height(48f)
                paddingLeft(4f)
                paddingRight(4f)
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
                        click {
                            safeRun("$tag 返回按钮") {
                                KLog.i(UI_LOG_TAG, "👆 [Click] $tag 返回按钮")
                                onBack()
                            }
                        }
                    }
                    Text {
                        attr {
                            text("‹")
                            fontSize(28f)
                            fontWeight700()
                            color(HelixTheme.current.onSurface)
                        }
                    }
                }
            }
            // 标题：flex(1) + 单行省略，避免被右侧操作挤变形
            View {
                attr {
                    flex(1f)
                    flexDirectionRow()
                    alignItemsCenter()
                    marginLeft(if (showBack) 4f else 12f)
                }
                Text {
                    attr {
                        text(title)
                        fontSize(18f)
                        fontWeight700()
                        color(HelixTheme.current.onSurface)
                        // 单行省略
                    }
                }
            }
            // 右侧操作：每个固定 40x40，避免挤压标题
            rightActions.forEach { action ->
                View {
                    attr {
                        width(40f)
                        height(40f)
                        justifyContentCenter()
                        alignItemsCenter()
                        borderRadius(20f)
                    }
                    event {
                        click {
                            safeRun("$tag 动作[${action.label}]") {
                                KLog.i(UI_LOG_TAG, "👆 [Click] $tag 动作[${action.label}]")
                                action.onClick()
                            }
                        }
                    }
                    Text {
                        attr {
                            text(action.label)
                            fontSize(20f)
                            color(HelixTheme.current.onSurface)
                        }
                    }
                }
            }
        }
        // 底部安全区
        View {
            attr {
                height(8f)
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
            backgroundColor(HelixTheme.current.surfaceContainerLow)
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
    val tag = componentTag("CardGroupRow")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [${item.title}]")
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            padding(14f)
            marginLeft(16f)
            marginRight(16f)
            marginTop(4f)
            marginBottom(4f)
            borderRadius(14f)
            backgroundColor(HelixTheme.current.surfaceContainerLow)
            alignSelfStretch()
        }
        if (item.icon.isNotEmpty()) {
            Text {
                attr {
                    text(item.icon)
                    fontSize(20f)
                    marginLeft(16f)
                    marginRight(12f)
                }
            }
        } else {
            View { attr { width(16f) } }
        }
        View {
            attr {
                flex(1f)
                flexDirectionColumn()
            }
            Text {
                attr {
                    text(item.title)
                    fontSize(16f)
                    color(HelixTheme.current.onSurface)
                }
            }
            if (item.subtitle.isNotEmpty()) {
                Text {
                    attr {
                        text(item.subtitle)
                        fontSize(13f)
                        color(HelixTheme.current.onSurfaceVariant)
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
                    color(HelixTheme.current.onSurfaceVariant)
                    marginRight(8f)
                }
            }
        }
        if (item.onClick != null) {
            Text {
                attr {
                    text("›")
                    fontSize(20f)
                    color(HelixTheme.current.onSurfaceVariant)
                    marginLeft(4f)
                    marginRight(16f)
                }
            }
        } else {
            View { attr { width(16f) } }
        }
        event {
            click {
                safeRun("$tag [${item.title}]") {
                    KLog.i(UI_LOG_TAG, "👆 [Click] $tag [${item.title}]")
                    item.onClick?.invoke()
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
                backgroundColor(HelixTheme.current.outlineVariant)
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
    val tag = componentTag("TextInput")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [${if (label.isNotEmpty()) label else placeholder}]")
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
                height(48f)
                borderRadius(8f)
                paddingLeft(12f)
                paddingRight(12f)
                justifyContentCenter()
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

// ==================== Tag ====================

private fun tagBackgroundColor(type: TagType): Color {
    return when (type) {
        TagType.SUCCESS -> Color(0xFFCDF6D9L)
        TagType.ERROR -> Color(0xFFFDCDC9L)
        TagType.WARNING -> Color(0xFFFFE4BBL)
        TagType.INFO -> Color(0xFFD6E4FFL)
        else -> HelixTheme.current.tertiaryContainer
    }
}

private fun tagTextColor(type: TagType): Color {
    return when (type) {
        TagType.SUCCESS -> Color(0xFF48A62EL)
        TagType.ERROR -> Color(0xFFCB2B2BL)
        TagType.WARNING -> Color(0xFFD8650CL)
        TagType.INFO -> Color(0xFF165DFFL)
        else -> HelixTheme.current.onTertiaryContainer
    }
}

fun ViewContainer<*, *>.Tag(
    text: String,
    type: TagType = TagType.DEFAULT,
    onClick: (() -> Unit)? = null
) {
    val tag = componentTag("Tag")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [$text]")
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
                click {
                    safeRun("$tag [$text]") {
                        KLog.i(UI_LOG_TAG, "👆 [Click] $tag [$text]")
                        handler()
                    }
                }
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
    checked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tag = componentTag("SwitchRow")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [$title] checked=${checked()}")
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
                    color(HelixTheme.current.onSurface)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text {
                    attr {
                        text(subtitle)
                        fontSize(14f)
                        color(HelixTheme.current.onSurfaceVariant)
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
                    if (checked()) HelixTheme.current.primary
                    else HelixTheme.current.surfaceContainerHighest
                )
                justifyContentCenter()
            }
            event {
                click {
                    safeRun("$tag [$title]") {
                        val newValue = !checked()
                        KLog.i(UI_LOG_TAG, "👆 [Click] $tag [$title] → $newValue")
                        onCheckedChange(newValue)
                    }
                }
            }
            View {
                attr {
                    width(22f)
                    height(22f)
                    borderRadius(11f)
                    backgroundColor(Color(0xFFFFFFFFL))
                    marginLeft(if (checked()) 22f else 2f)
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
    val tag = componentTag("ConfirmDialog")
    if (visible) {
        KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [$title]")
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
                click {
                    safeRun("$tag 遮罩取消") {
                        KLog.i(UI_LOG_TAG, "👆 [Click] $tag 遮罩取消")
                        onCancel()
                    }
                }
            }
            View {
                attr {
                    width(280f)
                    borderRadius(16f)
                    backgroundColor(HelixTheme.current.surfaceContainerHigh)
                    padding(24f)
                }
                Text {
                    attr {
                        text(title)
                        fontSize(18f)
                        color(HelixTheme.current.onSurface)
                        marginBottom(12f)
                    }
                }
                Text {
                    attr {
                        text(message)
                        fontSize(14f)
                        color(HelixTheme.current.onSurfaceVariant)
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
                            click {
                                safeRun("$tag 取消按钮") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $tag 取消按钮")
                                    onCancel()
                                }
                            }
                        }
                        Text {
                            attr {
                                text(cancelText)
                                fontSize(14f)
                                color(HelixTheme.current.primary)
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
                            click {
                                safeRun("$tag 确认按钮") {
                                    KLog.i(UI_LOG_TAG, "👆 [Click] $tag 确认按钮")
                                    onConfirm()
                                }
                            }
                        }
                        Text {
                            attr {
                                text(confirmText)
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
                color(HelixTheme.current.outline)
            }
        }
    }
}

// ==================== ErrorCard ====================

fun ViewContainer<*, *>.ErrorCard(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    val tag = componentTag("ErrorCard")
    KLog.i(UI_LOG_TAG, "🖼 [Render] $tag [$message]")
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
                    click {
                        safeRun("$tag 重试按钮") {
                            KLog.i(UI_LOG_TAG, "👆 [Click] $tag 重试按钮")
                            onRetry()
                        }
                    }
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
