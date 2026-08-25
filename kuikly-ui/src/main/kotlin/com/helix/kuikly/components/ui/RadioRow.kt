package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.compose.Button
import com.helix.kuikly.theme.HelixTheme

/**
 * 单选行（带 Radio 圆圈）
 *
 * 布局：左 emoji + 中标题副标题 + 右 Radio 圆圈
 * 选中 = 紫色实心圆；未选中 = 灰底 + 中心灰点
 *
 * 使用 Button 组件（ComposeView）确保 click 事件能正常触发
 */
fun ViewContainer<*, *>.RadioRow(
    title: String,
    subtitle: String = "",
    icon: String = "",
    selected: Boolean,
    onClick: () -> Unit
) {
    Button {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            padding(16f)
            alignSelfStretch()
        }
        if (icon.isNotEmpty()) {
            Text {
                attr {
                    text(icon)
                    fontSize(22f)
                    marginLeft(16f)
                    marginRight(12f)
                }
            }
        }
        View {
            attr {
                flex(1f)
                flexDirectionColumn()
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
                        fontSize(13f)
                        color(HelixTheme.Light.onSurfaceVariant)
                        marginTop(2f)
                    }
                }
            }
        }
        // Radio 圆圈（右侧）
        if (selected) {
            View {
                attr {
                    width(22f)
                    height(22f)
                    borderRadius(11f)
                    backgroundColor(HelixTheme.Light.primary)
                    marginRight(16f)
                    justifyContentCenter()
                    alignItemsCenter()
                }
                View {
                    attr {
                        width(8f)
                        height(8f)
                        borderRadius(4f)
                        backgroundColor(Color(0xFFFFFFFFL))
                    }
                }
            }
        } else {
            View {
                attr {
                    width(22f)
                    height(22f)
                    borderRadius(11f)
                    backgroundColor(Color(0xFFE0E0E0L))
                    marginRight(16f)
                    justifyContentCenter()
                    alignItemsCenter()
                }
                View {
                    attr {
                        width(8f)
                        height(8f)
                        borderRadius(4f)
                        backgroundColor(HelixTheme.Light.background)
                    }
                }
            }
        }
        // Button 的 click 事件
        event {
            click {
                onClick()
            }
        }
    }
}
