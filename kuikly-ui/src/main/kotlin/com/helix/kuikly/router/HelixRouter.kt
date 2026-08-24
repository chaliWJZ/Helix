package com.helix.kuikly.router

/**
 * Helix Kuikly 路由注册表
 *
 * 定义全部 56 个页面的路由路径，与 Compose Navigation2 的 Screen sealed interface 一一对应。
 * 迁移期间通过路由名称判断页面是否已迁移到 Kuikly：
 * - 已迁移的页面：跳转到 Kuikly 页面
 * - 未迁移的页面：继续走 Compose Navigation2
 */
object HelixRouter {

    // ==================== 路由路径常量 ====================
    // 与 RouteActivity.kt 中 Screen sealed interface 的 @Serializable data object 对齐

    // 核心页面
    const val CHAT = "chat"
    const val CHAT_DRAWER = "chat_drawer"

    // 设置页面（17 个）
    const val SETTING = "setting"
    const val SETTING_ABOUT = "setting_about"
    const val SETTING_PROVIDER = "setting_provider"
    const val SETTING_PROVIDER_DETAIL = "setting_provider_detail"
    const val SETTING_PREFERENCES = "setting_preferences"
    const val SETTING_PREFERENCES_UI = "setting_preferences_ui"
    const val SETTING_PREFERENCES_NETWORK = "setting_preferences_network"
    const val SETTING_MODEL = "setting_model"
    const val SETTING_MCP = "setting_mcp"
    const val SETTING_SPEECH = "setting_speech"
    const val SETTING_SEARCH = "setting_search"
    const val SETTING_BACKUP = "setting_backup"
    const val SETTING_FILES = "setting_files"
    const val SETTING_THEME = "setting_theme"
    const val SETTING_DONATE = "setting_donate"
    const val SETTING_ABOUT_LICENSE = "setting_about_license"
    const val SETTING_ABOUT_OPEN_SOURCE = "setting_about_open_source"

    // 助手页面（7 个）
    const val ASSISTANT = "assistant"
    const val ASSISTANT_DETAIL = "assistant_detail"
    const val ASSISTANT_DETAIL_BASIC = "assistant_detail_basic"
    const val ASSISTANT_DETAIL_PROMPT = "assistant_detail_prompt"
    const val ASSISTANT_DETAIL_KNOWLEDGE = "assistant_detail_knowledge"
    const val ASSISTANT_DETAIL_MODEL = "assistant_detail_model"
    const val ASSISTANT_DETAIL_TOOL = "assistant_detail_tool"

    // 扩展页面（8 个）
    const val EXTENSIONS = "extensions"
    const val EXTENSIONS_INSTALL = "extensions_install"
    const val EXTENSIONS_DETAIL = "extensions_detail"
    const val EXTENSIONS_SETTING = "extensions_setting"
    const val EXTENSIONS_INDEX = "extensions_index"

    // 其他页面
    const val HISTORY = "history"
    const val FAVORITE = "favorite"
    const val LOG = "log"
    const val STATS = "stats"
    const val SEARCH = "search"
    const val TRANSLATOR = "translator"
    const val IMGGEN = "imggen"
    const val WEBVIEW = "webview"
    const val BACKUP = "backup"
    const val DEBUG = "debug"
    const val SHARE_HANDLER = "share_handler"
    const val PROMPT = "prompt"
    const val QUICK_MESSAGES = "quick_messages"
    const val SKILLS = "skills"

    // Workspace 页面
    const val WORKSPACE = "workspace"
    const val WORKSPACE_TERMINAL = "workspace_terminal"
    const val WORKSPACE_FILE_EDITOR = "workspace_file_editor"

    // ==================== 已迁移到 Kuikly 的页面注册表 ====================
    // 每迁移一个页面，在此处注册（渐进式迁移）

    private val migratedPages = mutableSetOf<String>()

    fun registerMigrated(route: String) {
        migratedPages.add(route)
    }

    fun isMigrated(route: String): Boolean {
        return route in migratedPages
    }

    fun getMigratedPages(): Set<String> {
        return migratedPages.toSet()
    }

    // ==================== 全部路由列表 ====================

    val allRoutes: List<String> = listOf(
        CHAT, CHAT_DRAWER,
        SETTING, SETTING_ABOUT, SETTING_PROVIDER, SETTING_PROVIDER_DETAIL,
        SETTING_PREFERENCES, SETTING_PREFERENCES_UI, SETTING_PREFERENCES_NETWORK,
        SETTING_MODEL, SETTING_MCP, SETTING_SPEECH, SETTING_SEARCH,
        SETTING_BACKUP, SETTING_FILES, SETTING_THEME, SETTING_DONATE,
        SETTING_ABOUT_LICENSE, SETTING_ABOUT_OPEN_SOURCE,
        ASSISTANT, ASSISTANT_DETAIL, ASSISTANT_DETAIL_BASIC, ASSISTANT_DETAIL_PROMPT,
        ASSISTANT_DETAIL_KNOWLEDGE, ASSISTANT_DETAIL_MODEL, ASSISTANT_DETAIL_TOOL,
        EXTENSIONS, EXTENSIONS_INSTALL, EXTENSIONS_DETAIL, EXTENSIONS_SETTING, EXTENSIONS_INDEX,
        HISTORY, FAVORITE, LOG, STATS, SEARCH, TRANSLATOR, IMGGEN, WEBVIEW,
        BACKUP, DEBUG, SHARE_HANDLER, PROMPT, QUICK_MESSAGES, SKILLS,
        WORKSPACE, WORKSPACE_TERMINAL, WORKSPACE_FILE_EDITOR
    )
}
