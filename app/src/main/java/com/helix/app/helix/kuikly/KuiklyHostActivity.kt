package com.helix.app.helix.kuikly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.KuiklyRenderView
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.performace.KRMonitorType
import org.json.JSONObject

/**
 * Helix Kuikly 宿主 Activity
 *
 * 承载 Kuikly 页面的原生容器：
 * 1. 创建 KuiklyRenderViewBaseDelegator 并注册 HelixRenderModule
 * 2. 通过 delegator.onAttach 打开指定 Kuikly 页面
 * 3. 转发生命周期与返回键事件
 *
 * 用法：KuiklyHostActivity.start(context, "SettingPage", jsonObject)
 */
class KuiklyHostActivity : Activity() {

    private lateinit var containerView: FrameLayout
    private lateinit var delegator: KuiklyRenderViewBaseDelegator

    private val pageName: String
        get() = intent.getStringExtra(KEY_PAGE_NAME) ?: DEFAULT_PAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImmersiveMode()
        setupAdapterManager()

        // 容器 View（代码创建，无需 XML 布局）
        containerView = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(containerView)

        // 1. 创建 Delegator（注册 Module 的入口）
        delegator = KuiklyRenderViewBaseDelegator(object : KuiklyRenderViewBaseDelegatorDelegate {
            override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
                super.registerExternalModule(kuiklyRenderExport)
                with(kuiklyRenderExport) {
                    moduleExport(HelixRenderModule.MODULE_NAME) { HelixRenderModule() }
                }
            }

            override fun coreExecuteModeX(): KuiklyRenderCoreExecuteModeBase {
                return KuiklyRenderCoreExecuteModeBase.JVM
            }

            override fun performanceMonitorTypes(): List<KRMonitorType> {
                return listOf(KRMonitorType.LAUNCH)
            }
        })

        // 2. 打开 Kuikly 页面
        delegator.onAttach(containerView, "", pageName, createPageData())
    }

    override fun onResume() {
        super.onResume()
        delegator.onResume()
    }

    override fun onPause() {
        super.onPause()
        delegator.onPause()
    }

    override fun onDestroy() {
        delegator.onDetach()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (delegator.onBackPressed()) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun createPageData(): Map<String, Any> {
        val data = mutableMapOf<String, Any>()
        intent.getStringExtra(KEY_PAGE_DATA)?.let { jsonStr ->
            runCatching {
                val json = JSONObject(jsonStr)
                json.keys().forEach { key -> data[key] = json.get(key) }
            }
        }
        data["sysLang"] = resources.configuration.locale.language
        return data
    }

    private fun setupAdapterManager() {
        // 路由适配器（页面间跳转）
        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = HelixRouterAdapter()
        }
    }

    private fun setupImmersiveMode() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
        }
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    companion object {
        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"
        private const val DEFAULT_PAGE = "SettingPage"

        fun start(context: Context, pageName: String, pageData: JSONObject? = null) {
            val intent = Intent(context, KuiklyHostActivity::class.java)
            intent.putExtra(KEY_PAGE_NAME, pageName)
            pageData?.let { intent.putExtra(KEY_PAGE_DATA, it.toString()) }
            context.startActivity(intent)
        }
    }
}
