package com.helix.kuikly.theme

import com.tencent.kuikly.core.reactive.handler.observable

/**
 * Helix 主题管理器（全局单例）
 *
 * 管理当前明/暗主题状态，所有 Kuikly 页面通过 HelixTheme.current 间接读取颜色。
 * 主题切换时（isDark observable 变化），所有引用 current.xxx 的 attr 块自动重新计算并刷新 UI。
 *
 * 流程：
 * 1. 原生 Activity 读取 Configuration.UI_MODE_NIGHT_* → 传 pageData.sysTheme
 * 2. HelixBasePage.created() 读 sysTheme → ThemeManager.isDark = ...
 * 3. 系统切换暗色 → Activity recreate → 重新走 1-2 流程
 */
object ThemeManager {

    /** 当前是否暗色主题（observable，attr{} 内引用会自动订阅） */
    var isDark by observable(false)

    /** 跟随系统开关：开启时跟随系统切，关闭时由用户手动选择 */
    var followSystem by observable(true)

    /**
     * 初始化：在 HelixBasePage.created() 调用
     * @param sysTheme 原生传入的 sysTheme："light"/"dark"/"unspecified"
     */
    fun initFromSystem(sysTheme: String?) {
        val newDark = when (sysTheme?.lowercase()) {
            "dark" -> true
            "light" -> false
            else -> isDark  // "unspecified" 或 null 保持现状
        }
        if (isDark != newDark) {
            isDark = newDark
        }
    }
}
