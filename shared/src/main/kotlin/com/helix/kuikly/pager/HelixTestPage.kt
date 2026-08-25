package com.helix.kuikly.pager

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * Kuikly 测试页面：验证 kuikly-ui 模块基础设施搭建成功
 */
@Page("HelixTestPage")
internal class HelixTestPage : Pager() {
    override fun body(): ViewBuilder {
        return {
            View {
                attr {
                    flex(1f)
                    justifyContentCenter()
                    alignItemsCenter()
                }
                Text {
                    attr {
                        text("Helix Kuikly Ready!")
                        fontSize(18f)
                    }
                }
            }
        }
    }
}
