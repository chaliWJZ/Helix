package com.helix.kuikly.components.ui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.log.KLog
import com.helix.kuikly.theme.HelixTheme

/**
 * 单选行（带 Radio 圆圈）
 *
 * 布局：左 emoji + 中标题副标题 + 右 Radio 圆圈
 * 选中 = 紫色实心圆；未选中 = 灰底 + 中心灰点
 *
 * 用普通 View + event.click（与 TestHub 入口同款可命中的写法）
 */
fun ViewContainer<*, *>.RadioRow(
    title: String,
    subtitle: String = "",
    icon: String = "",
    selected: Boolean,
    onClick: () -> Unit
) {
    KLog.i("HelixKuikly", "🖼 [Render] 单选[$title] selected=$selected")
    View {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            marginLeft(16f)
            marginRight(16f)
            marginTop(4f)
            marginBottom(4f)
            padding(14f)
            borderRadius(14f)
            // 与 TestHub 入口一致：有背景色保证 hit-test 命中
            backgroundColor(
                if (selected) HelixTheme.current.primaryContainer
                else HelixTheme.current.surfaceContainerLow
            )
            alignSelfStretch()
        }
        event {
            click {
                safeRun("单选[$title") {
                    KLog.i("HelixKuikly", "👆 [Click] 单选[$title]")
                    onClick()
            
                }
            
            }
        }
        if (icon.isNotEmpty()) {
            Text {
                attr {
                    text(icon)
                    fontSize(22f)
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
                    color(HelixTheme.current.onSurface)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text {
                    attr {
                        text(subtitle)
                        fontSize(13f)
                        color(HelixTheme.current.onSurfaceVariant)
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
                    backgroundColor(HelixTheme.current.primary)
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
                    justifyContentCenter()
                    alignItemsCenter()
                }
                View {
                    attr {
                        width(8f)
                        height(8f)
                        borderRadius(4f)
                        backgroundColor(HelixTheme.current.background)
                    }
                }
            }
        }
    }
}
