package com.helix.app.helix.kuikly

import android.app.Activity
import android.content.Context
import android.util.Log
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import org.json.JSONObject

/**
 * Helix Kuikly 路由适配器
 *
 * 处理 Kuikly 页面内的 RouterModule.openPage/closePage 调用：
 * - openPage → 复用当前 KuiklyHostActivity（切换页面），避免栈无限增长
 * - closePage → finish 当前宿主 Activity
 */
class HelixRouterAdapter : IKRRouterAdapter {

    override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
        Log.i(TAG, "🔀 [Router-Open] $pageName data=${pageData.toString().take(200)}")
        // 如果当前 Activity 本身就是 KuiklyHostActivity，复用并切换页面
        if (context is KuiklyHostActivity) {
            context.openPage(pageName, pageData)
        } else {
            // 从外部启动（极少见）：新建 Activity
            KuiklyHostActivity.start(context, pageName, pageData)
        }
    }

    override fun closePage(context: Context) {
        Log.i(TAG, "🔀 [Router-Close] 关闭当前页面（走页面栈）")
        (context as? KuiklyHostActivity)?.backOrFinish()
            ?: (context as? Activity)?.finish()
    }

    companion object {
        private const val TAG = "HelixKuikly"
    }
}
