package com.helix.app.helix.kuikly

import android.app.Activity
import android.content.Context
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import org.json.JSONObject

/**
 * Helix Kuikly 路由适配器
 *
 * 处理 Kuikly 页面内的 RouterModule.openPage/closePage 调用：
 * - openPage → 打开新的 KuiklyHostActivity（承载目标 Kuikly 页面）
 * - closePage → finish 当前宿主 Activity
 */
class HelixRouterAdapter : IKRRouterAdapter {

    override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
        KuiklyHostActivity.start(context, pageName, pageData)
    }

    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }
}
