package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 思维链展示（可折叠/展开）
 */
fun ViewContainer<*, *>.ChainOfThought(
    content: String,
    expanded: Boolean = false,
    onToggle: () -> Unit = {},
    thinkingTime: String = ""
) {
    View {
        attr {
            borderRadius(12f)
            backgroundColor(HelixTheme.current.surfaceContainerLow)
            padding(12f)
            marginBottom(8f)
        }
        View {
            attr { flexDirectionRow(); alignItemsCenter() }
            event { click { KLog.i("HelixKuikly", "👆 [Click] 思维链折叠"); onToggle() } }
            Text { attr { text("💭 思考中..."); fontSize(14f); color(HelixTheme.current.onSurfaceVariant); flex(1f) } }
            if (thinkingTime.isNotEmpty()) {
                Text { attr { text(thinkingTime); fontSize(12f); color(HelixTheme.current.outline) } }
            }
            Text { attr { text(if (expanded) "▲" else "▼"); fontSize(12f); color(HelixTheme.current.onSurfaceVariant); marginLeft(8f) } }
        }
        if (expanded && content.isNotEmpty()) {
            View { attr { marginTop(8f); paddingTop(8f) } }
            Text { attr { text(content); fontSize(14f); color(HelixTheme.current.onSurfaceVariant) } }
        }
    }
}

/**
 * 标签列表（横向排列，可换行）
 */
fun ViewContainer<*, *>.TagList(
    tags: List<Pair<String, TagType>>,
    maxTags: Int = -1,
    onTagClick: ((Int) -> Unit)? = null
) {
    View {
        attr { flexDirectionRow(); flexWrapWrap(); marginTop(4f); marginBottom(4f) }
        val displayTags = if (maxTags > 0 && tags.size > maxTags) tags.take(maxTags) else tags
        displayTags.forEachIndexed { index, (text, type) ->
            Tag(text = text, type = type, onClick = onTagClick?.let { h -> { h(index) } })
            View { attr { width(4f); height(4f) } }
        }
        if (maxTags > 0 && tags.size > maxTags) {
            Tag(text = "+${tags.size - maxTags}", type = TagType.DEFAULT)
        }
    }
}

/**
 * 更新提示卡片
 */
fun ViewContainer<*, *>.UpdateCard(
    version: String,
    description: String,
    onUpdate: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    KLog.i("HelixKuikly", "🖼 [Render] 更新卡片[v$version]")
    View {
        attr {
            borderRadius(16f); backgroundColor(HelixTheme.current.primaryContainer)
            padding(16f); marginLeft(16f); marginRight(16f); marginBottom(8f)
        }
        View {
            attr { flexDirectionRow(); alignItemsCenter() }
            Text { attr { text("🎉 发现新版本 v$version"); fontSize(16f); color(HelixTheme.current.onPrimaryContainer); flex(1f) } }
            View {
                attr { paddingLeft(8f); paddingRight(8f) }
                event { click { KLog.i("HelixKuikly", "👆 [Click] 更新卡片关闭"); onDismiss() } }
                Text { attr { text("✕"); fontSize(14f); color(HelixTheme.current.onPrimaryContainer) } }
            }
        }
        if (description.isNotEmpty()) {
            Text { attr { text(description); fontSize(14f); color(HelixTheme.current.onPrimaryContainer); marginTop(8f) } }
        }
        View {
            attr {
                marginTop(12f); paddingTop(8f); paddingBottom(8f)
                paddingLeft(16f); paddingRight(16f)
                borderRadius(20f); backgroundColor(HelixTheme.current.primary); alignSelfFlexStart()
            }
            event { click { KLog.i("HelixKuikly", "👆 [Click] 更新卡片立即更新"); onUpdate() } }
            Text { attr { text("立即更新"); fontSize(14f); color(HelixTheme.current.onPrimary) } }
        }
    }
}

/**
 * 问候语组件（根据时间显示不同问候）
 */
fun ViewContainer<*, *>.Greeting(userName: String = "", hour: Int = -1) {
    val greeting = when {
        hour in 5..11 -> "早上好"
        hour in 12..13 -> "中午好"
        hour in 14..17 -> "下午好"
        hour in 18..22 -> "晚上好"
        else -> "夜深了"
    }
    View {
        attr { paddingLeft(16f); paddingRight(16f); paddingTop(16f); paddingBottom(8f) }
        Text { attr { text(if (userName.isNotEmpty()) "$greeting，$userName" else greeting); fontSize(28f); color(HelixTheme.current.onBackground) } }
        Text { attr { text("今天想聊些什么？"); fontSize(14f); color(HelixTheme.current.onSurfaceVariant); marginTop(4f) } }
    }
}

/**
 * 可选中列表项
 */
fun ViewContainer<*, *>.ListSelectableItem(
    title: String,
    subtitle: String = "",
    selected: Boolean,
    onClick: () -> Unit
) {
    KLog.i("HelixKuikly", "🖼 [Render] 可选项[$title] selected=$selected")
    View {
        attr {
            flexDirectionRow(); alignItemsCenter(); padding(14f); paddingLeft(16f)
            backgroundColor(if (selected) HelixTheme.current.secondaryContainer else Color(0x00000000L))
        }
        event { click { KLog.i("HelixKuikly", "👆 [Click] 可选项[$title]"); onClick() } }
        // 选择指示器（用背景色切换替代边框）
        View {
            attr {
                width(20f); height(20f); borderRadius(10f)
                backgroundColor(if (selected) HelixTheme.current.primary else HelixTheme.current.surfaceContainerHighest)
                justifyContentCenter(); alignItemsCenter(); marginRight(12f)
            }
            if (selected) {
                View { attr { width(10f); height(10f); borderRadius(5f); backgroundColor(Color(0xFFFFFFFFL)) } }
            }
        }
        View {
            attr { flex(1f) }
            Text { attr { text(title); fontSize(16f); color(if (selected) HelixTheme.current.onSecondaryContainer else HelixTheme.current.onSurface) } }
            if (subtitle.isNotEmpty()) {
                Text { attr { text(subtitle); fontSize(12f); color(HelixTheme.current.onSurfaceVariant); marginTop(2f) } }
            }
        }
    }
}

/**
 * JSON 树形展示（简化版：直接显示格式化 JSON）
 */
fun ViewContainer<*, *>.JsonTree(json: String, level: Int = 0) {
    View {
        attr { paddingLeft(level * 16f) }
        Text { attr { text(json); fontSize(12f); color(HelixTheme.current.onSurfaceVariant) } }
    }
}
