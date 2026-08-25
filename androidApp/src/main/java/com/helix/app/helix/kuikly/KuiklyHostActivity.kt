package com.helix.app.helix.kuikly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private var pageLoadStartTime = 0L

    private var pageName: String
        get() = intent.getStringExtra(KEY_PAGE_NAME) ?: DEFAULT_PAGE
        set(value) {
            intent.putExtra(KEY_PAGE_NAME, value)
        }

    /** 页面栈（跨 recreate 持久化在 intent 里），用于返回导航 */
    private var pageStack: ArrayList<String>
        get() = intent.getStringArrayListExtra(KEY_PAGE_STACK) ?: arrayListOf()
        set(value) {
            intent.putExtra(KEY_PAGE_STACK, value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageLoadStartTime = System.currentTimeMillis()
        Log.i(TAG, "▶ [Page-Open] pageName=$pageName")
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
                Log.i(TAG, "✓ [Module-Register] HelixBridgeModule 注册成功")
            }

            override fun coreExecuteModeX(): KuiklyRenderCoreExecuteModeBase {
                return KuiklyRenderCoreExecuteModeBase.JVM
            }

            override fun performanceMonitorTypes(): List<KRMonitorType> {
                return listOf(KRMonitorType.LAUNCH)
            }

            override fun onKuiklyRenderViewCreated() {
                super.onKuiklyRenderViewCreated()
                Log.i(TAG, "✓ [RenderView-Created] 引擎视图创建完成")
            }

            override fun onKuiklyRenderContentViewCreated() {
                super.onKuiklyRenderContentViewCreated()
                Log.i(
                    TAG,
                    "✓ [ContentView-Created] 页面内容创建，耗时=${System.currentTimeMillis() - pageLoadStartTime}ms"
                )
            }

            override fun onPageLoadComplete(
                isSucceed: Boolean,
                errorReason: com.tencent.kuikly.core.render.android.exception.ErrorReason?,
                executeMode: KuiklyRenderCoreExecuteModeBase
            ) {
                super.onPageLoadComplete(isSucceed, errorReason, executeMode)
                if (isSucceed) {
                    Log.i(
                        TAG,
                        "✅ [Page-Load-OK] $pageName 加载成功，总耗时=${System.currentTimeMillis() - pageLoadStartTime}ms"
                    )
                } else {
                    Log.e(
                        TAG,
                        "❌ [Page-Load-Fail] $pageName 加载失败！reason=$errorReason"
                    )
                }
            }

            override fun onUnhandledException(
                throwable: Throwable,
                errorReason: com.tencent.kuikly.core.render.android.exception.ErrorReason,
                executeMode: KuiklyRenderCoreExecuteModeBase
            ) {
                super.onUnhandledException(throwable, errorReason, executeMode)
                Log.e(
                    TAG,
                    "💥 [Page-Crash] $pageName 页面异常！reason=$errorReason\n${throwable.stackTraceToString().take(2000)}"
                )
            }
        })

        // 2. 打开 Kuikly 页面
        Log.d(TAG, "… [Page-Attach] 开始挂载页面: $pageName")
        delegator.onAttach(containerView, "", pageName, createPageData())
    }

    override fun onResume() {
        super.onResume()
        delegator.onResume()
        Log.d(TAG, "▶ [Lifecycle] onResume: $pageName")
    }

    override fun onPause() {
        super.onPause()
        delegator.onPause()
        Log.d(TAG, "▶ [Lifecycle] onPause: $pageName")
    }

    override fun onDestroy() {
        delegator.onDetach()
        Log.i(TAG, "■ [Lifecycle] onDestroy: $pageName 页面销毁")
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // 系统日夜间切换：recreate 让 Kuikly 页面重建并读取新 sysTheme
        // （仅跟随系统模式下需要；用户手动选了浅/深色时 ThemeManager.followSystem=false 也不会被覆盖——重建时仍读 sysTheme，
        //   简化处理：重建即跟随系统。手动模式持久化接入后改为按用户选择恢复）
        val newNight = (newConfig.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        Log.i(TAG, "🌗 [Theme-Change] 系统日夜间变化 → $newNight，recreate 跟随")
        recreate()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            val handled = delegator.onBackPressed()
            Log.d(TAG, "▶ [Back-Press] $pageName 返回键, Kuikly处理=$handled")
            if (!handled) {
                // Kuikly 未拦截（如无弹窗/键盘），走页面栈返回
                backOrFinish()
            }
            return true
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
        // 告诉 Kuikly 页面当前系统是 light/dark（用于主题跟随）
        val isNight = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        data["sysTheme"] = if (isNight) "dark" else "light"
        return data
    }

    private fun setupAdapterManager() {
        // 路由适配器（页面间跳转）
        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = HelixRouterAdapter()
        }
        // 日志适配器：KLog → Android logcat（TAG 原样转发，可被 -s HelixKuikly 过滤）
        if (KuiklyRenderAdapterManager.krLogAdapter == null) {
            KuiklyRenderAdapterManager.krLogAdapter = object :
                com.tencent.kuikly.core.render.android.adapter.IKRLogAdapter {
                override val asyncLogEnable: Boolean get() = true
                override fun i(tag: String, msg: String) { Log.i(tag, msg) }
                override fun d(tag: String, msg: String) { Log.d(tag, msg) }
                override fun e(tag: String, msg: String) { Log.e(tag, msg) }
            }
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
        private const val TAG = "HelixKuikly"
        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"
        private const val KEY_PAGE_STACK = "pageStack"
        private const val DEFAULT_PAGE = "TestHubPage"

        fun start(context: Context, pageName: String, pageData: JSONObject? = null) {
            val intent = Intent(context, KuiklyHostActivity::class.java)
            intent.putExtra(KEY_PAGE_NAME, pageName)
            pageData?.let { intent.putExtra(KEY_PAGE_DATA, it.toString()) }
            context.startActivity(intent)
        }
    }

    /**
     * 在当前 Activity 内打开新页面（由 HelixRouterAdapter.openPage 调用）
     * 当前页压入页面栈后切换到新页面，返回时可恢复
     */
    fun openPage(pageName: String, pageData: JSONObject? = null) {
        Log.i(TAG, "🔀 [Page-Push] $pageName（栈深度 ${pageStack.size + 1}）")
        val stack = pageStack
        stack.add(this.pageName)  // 当前页压栈
        pageStack = stack
        intent.putExtra(KEY_PAGE_NAME, pageName)
        if (pageData != null) {
            intent.putExtra(KEY_PAGE_DATA, pageData.toString())
        } else {
            intent.removeExtra(KEY_PAGE_DATA)
        }
        recreate()
    }

    /**
     * 返回上一页：栈非空弹栈恢复，栈空 finish 退出
     */
    fun backOrFinish() {
        val stack = pageStack
        if (stack.isNotEmpty()) {
            val previous = stack.removeAt(stack.lastIndex)
            pageStack = stack
            Log.i(TAG, "🔀 [Page-Pop] 返回 $previous（剩余栈深度 ${stack.size}）")
            intent.putExtra(KEY_PAGE_NAME, previous)
            intent.removeExtra(KEY_PAGE_DATA)
            recreate()
        } else {
            Log.i(TAG, "🔀 [Page-Exit] 栈空，退出")
            finish()
        }
    }
}
