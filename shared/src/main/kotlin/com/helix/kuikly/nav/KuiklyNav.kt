package com.helix.kuikly.nav

import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import java.lang.ref.WeakReference

/**
 * Kuikly 公共跳转工具
 *
 * 统一所有页面间跳转：传入页面名（+可选参数）即可跳转。
 * 原生侧 KuiklyHostActivity 维护页面栈，closePage 自动返回上一页。
 *
 * 用法：
 * ```
 * KuiklyNav.open("SettingPage")                        // 跳转
 * KuiklyNav.open("WebViewPage", mapOf("url" to "https://github.com"))  // 带参数
 * KuiklyNav.close(this)                                // 返回上一页
 * ```
 */
object KuiklyNav {

    /** 全部页面注册表：页面名 → 中文标题 */
    val PAGES: Map<String, String> = linkedMapOf(
        // ===== 测试中枢 =====
        "TestHubPage" to "测试导航中枢",

        // ===== 聊天系 =====
        "ChatPage" to "聊天页",
        "ChatDrawerPage" to "会话抽屉页",
        "MarkdownDemoPage" to "Markdown演示页",
        "WebViewPage" to "网页页",

        // ===== 设置系 =====
        "SettingPage" to "设置页",
        "SettingPreferencesPage" to "偏好设置页",
        "SettingPreferencesThemePage" to "主题偏好页",
        "SettingPreferencesNotificationPage" to "通知偏好页",
        "SettingPreferencesGeneralPage" to "通用偏好页",
        "SettingPreferencesUIPage" to "界面偏好页",
        "SettingPreferencesNetworkPage" to "网络偏好页",
        "SettingModelPage" to "默认模型页",
        "SettingModelPromptPage" to "提示词模板页",
        "SettingProviderPage" to "供应商列表页",
        "SettingProviderDetailPage" to "供应商详情页",
        "SettingSearchPage" to "搜索服务页",
        "SettingSearchDetailPage" to "搜索详情页",
        "SettingSpeechPage" to "TTS服务页",
        "TTSProviderConfigurePage" to "TTS配置页",
        "SettingMcpPage" to "MCP设置页",
        "SettingWebPage" to "Web服务器页",
        "SettingAboutPage" to "关于页",
        "SettingAboutLicensePage" to "开源许可页",
        "SettingDonatePage" to "捐赠页",
        "SettingFilesPage" to "文件管理页",
        "SettingThemePage" to "主题设置页",

        // ===== 列表系 =====
        "HistoryPage" to "历史记录页",
        "FavoritePage" to "收藏页",
        "AssistantPage" to "助手列表页",
        "AssistantDetailPage" to "助手详情页",
        "ExtensionsPage" to "扩展页",
        "SkillDetailPage" to "技能详情页",
        "PromptPage" to "提示词页",
        "QuickMessagesPage" to "快捷消息页",
        "SkillsPage" to "技能列表页",
        "LogPage" to "日志页",
        "StatsPage" to "统计页",

        // ===== 功能系 =====
        "BackupPage" to "备份页",
        "DebugPage" to "调试页",
        "TranslatorPage" to "翻译页",
        "ImgGenPage" to "图片生成页",
        "SearchPage" to "搜索页",
        "ShareHandlerPage" to "分享处理页",

        // ===== 工作区系 =====
        "WorkspacePage" to "工作区列表页",
        "WorkspaceDetailPage" to "工作区详情页",
        "WorkspaceTerminalPage" to "终端页",
        "WorkspaceFileEditorPage" to "文件编辑页"
    )

    /** 按分类组织的页面清单（供 TestHubPage 展示） */
    val CATEGORIES: List<Triple<String, String, List<String>>> = listOf(
        Triple("💬", "聊天系", listOf(
            "ChatPage", "ChatDrawerPage", "MarkdownDemoPage", "WebViewPage"
        )),
        Triple("⚙️", "设置系", listOf(
            "SettingPage", "SettingPreferencesPage", "SettingPreferencesThemePage",
            "SettingPreferencesNotificationPage", "SettingPreferencesGeneralPage",
            "SettingPreferencesUIPage", "SettingPreferencesNetworkPage",
            "SettingModelPage", "SettingModelPromptPage",
            "SettingProviderPage", "SettingProviderDetailPage",
            "SettingSearchPage", "SettingSearchDetailPage",
            "SettingSpeechPage", "TTSProviderConfigurePage",
            "SettingMcpPage", "SettingWebPage",
            "SettingAboutPage", "SettingDonatePage", "SettingFilesPage", "SettingThemePage"
        )),
        Triple("📋", "列表系", listOf(
            "HistoryPage", "FavoritePage", "AssistantPage", "AssistantDetailPage",
            "ExtensionsPage", "SkillDetailPage", "PromptPage",
            "QuickMessagesPage", "SkillsPage", "LogPage", "StatsPage"
        )),
        Triple("🔧", "功能系", listOf(
            "BackupPage", "DebugPage", "TranslatorPage", "ImgGenPage",
            "SearchPage", "ShareHandlerPage"
        )),
        Triple("📁", "工作区系", listOf(
            "WorkspacePage", "WorkspaceDetailPage", "WorkspaceTerminalPage", "WorkspaceFileEditorPage"
        ))
    )

    /** 打开页面（带可选参数） */
    fun open(pageName: String, params: Map<String, Any>? = null) {
        val pager = currentPagerRef?.get()
        val router = pager?.acquireModule<RouterModule>(RouterModule.MODULE_NAME)
        val json = params?.let {
            JSONObject().apply { it.forEach { (k, v) -> put(k, v) } }
        }
        router?.openPage(pageName, json)
    }

    /** 关闭当前页（返回上一页） */
    fun close(pager: Pager) {
        pager.acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
    }

    /**
     * 当前 Pager 引用（由 HelixBasePage 维护）
     *
     * WeakReference 弱引用：单例不阻止页面被 GC（修复内存泄漏 stbug-kzSX6QIAbP/stbug-nFDZxdC8g9）。
     * 页面销毁后 get() 返回 null，open() 静默不跳转（页面已销毁无需跳转）。
     */
    private var currentPagerRef: WeakReference<Pager>? = null

    /** 供 HelixBasePage.created() 登记（弱引用包装） */
    fun setCurrentPager(pager: Pager) {
        currentPagerRef = WeakReference(pager)
    }

    /** 当前 Pager（可能已回收返回 null） */
    val currentPager: Pager?
        get() = currentPagerRef?.get()

    /** 页面名 → 中文标题（未知页面返回页面名本身） */
    fun title(pageName: String): String = PAGES[pageName] ?: pageName
}
