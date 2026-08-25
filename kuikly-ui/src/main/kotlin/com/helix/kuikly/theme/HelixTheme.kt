package com.helix.kuikly.theme

import com.tencent.kuikly.core.base.Color

/**
 * Helix 主题系统：Material3 色彩体系的 Kuikly 映射
 * 
 * 颜色格式：Color(0xAARRGGBB)，与 Compose 的 Color(Long) 对齐
 * 
 * 色彩参考来源：
 * - Compose lightColorScheme() 默认值（Material3 baseline）
 * - app/ui/theme/Color.kt 的 ExtendColors 扩展色板
 */
object HelixTheme {

    /**
     * 动态主题入口：跟随 ThemeManager.isDark 切换
     * 页面 attr{} 内引用会自动订阅 isDark 实现响应式刷新
     */
    val current: ThemeColors
        get() = if (ThemeManager.isDark) Dark else Light

    interface ThemeColors {
        val primary: Color
        val onPrimary: Color
        val primaryContainer: Color
        val onPrimaryContainer: Color
        val secondary: Color
        val onSecondary: Color
        val secondaryContainer: Color
        val onSecondaryContainer: Color
        val tertiary: Color
        val onTertiary: Color
        val tertiaryContainer: Color
        val onTertiaryContainer: Color
        val background: Color
        val onBackground: Color
        val surface: Color
        val onSurface: Color
        val surfaceVariant: Color
        val onSurfaceVariant: Color
        val surfaceTint: Color
        val surfaceContainerLowest: Color
        val surfaceContainerLow: Color
        val surfaceContainer: Color
        val surfaceContainerHigh: Color
        val surfaceContainerHighest: Color
        val inverseSurface: Color
        val inverseOnSurface: Color
        val inversePrimary: Color
        val error: Color
        val onError: Color
        val errorContainer: Color
        val onErrorContainer: Color
        val outline: Color
        val outlineVariant: Color
        val scrim: Color
        val red: Color
        val orange: Color
        val gold: Color
        val lime: Color
        val green: Color
        val cyan: Color
        val blue: Color
        val arcoblue: Color
        val purple: Color
        val magenta: Color
        val gray: Color
    }

    // ==================== Material3 核心色彩（亮色模式） ====================
    
    object Light : ThemeColors {
    override val primary = Color(0xFF6750A4L)
    override val onPrimary = Color(0xFFFFFFFFL)
    override val primaryContainer = Color(0xFFEADDFFL)
    override val onPrimaryContainer = Color(0xFF21005DL)
        
    override val secondary = Color(0xFF625B71L)
    override val onSecondary = Color(0xFFFFFFFFL)
    override val secondaryContainer = Color(0xFFE8DEF8L)
    override val onSecondaryContainer = Color(0xFF1D192BL)
        
    override val tertiary = Color(0xFF7D5260L)
    override val onTertiary = Color(0xFFFFFFFFL)
    override val tertiaryContainer = Color(0xFFFFD8E4L)
    override val onTertiaryContainer = Color(0xFF31111DL)
        
    override val background = Color(0xFFFFFBFEL)
    override val onBackground = Color(0xFF1C1B1FL)
        
    override val surface = Color(0xFFFFFBFEL)
    override val onSurface = Color(0xFF1C1B1FL)
    override val surfaceVariant = Color(0xFFE7E0ECL)
    override val onSurfaceVariant = Color(0xFF49454FL)
    override val surfaceTint = primary
        
    override val surfaceContainerLowest = Color(0xFFFFFFFFL)
    override val surfaceContainerLow = Color(0xFFF7F2FAL)
    override val surfaceContainer = Color(0xFFF3EDF7L)
    override val surfaceContainerHigh = Color(0xFFECE6F0L)
    override val surfaceContainerHighest = Color(0xFFE6E0E9L)
        
    override val inverseSurface = Color(0xFF313033L)
    override val inverseOnSurface = Color(0xFFF4EFF4L)
    override val inversePrimary = Color(0xFFD0BCFFL)
        
    override val error = Color(0xFFB3261EL)
    override val onError = Color(0xFFFFFFFFL)
    override val errorContainer = Color(0xFFF9DECCL)
    override val onErrorContainer = Color(0xFF410E0BL)
        
    override val outline = Color(0xFF79747EL)
    override val outlineVariant = Color(0xFFCAC4D0L)
    override val scrim = Color(0xFF000000L)
        
        // ==================== 扩展色板（ExtendColors 精选） ====================
    override val red = Color(0xFFF53F3FL)
    override val orange = Color(0xFFFA8C16L)
    override val gold = Color(0xFFFADC60L)
    override val lime = Color(0xFFA8E10CL)
    override val green = Color(0xFF00B42AL)
    override val cyan = Color(0xFF14C9C9L)
    override val blue = Color(0xFF165DFFL)
    override val arcoblue = Color(0xFF00A3F0L)
    override val purple = Color(0xFF722ED1L)
    override val magenta = Color(0xFFF77AB0L)
    override val gray = Color(0xFF86909CL)
    }
    
    // ==================== Material3 核心色彩（暗色模式） ====================
    
    object Dark : ThemeColors {
    override val primary = Color(0xFFD0BCFFL)
    override val onPrimary = Color(0xFF381E72L)
    override val primaryContainer = Color(0xFF4F378BL)
    override val onPrimaryContainer = Color(0xFFEADDFFL)
        
    override val secondary = Color(0xFFCCC2DCL)
    override val onSecondary = Color(0xFF332D41L)
    override val secondaryContainer = Color(0xFF4A4458L)
    override val onSecondaryContainer = Color(0xFFE8DEF8L)
        
    override val tertiary = Color(0xFFEFB8C8L)
    override val onTertiary = Color(0xFF492532L)
    override val tertiaryContainer = Color(0xFF633B48L)
    override val onTertiaryContainer = Color(0xFFFFD8E4L)
        
    override val background = Color(0xFF1C1B1FL)
    override val onBackground = Color(0xFFE6E1E5L)
        
    override val surface = Color(0xFF1C1B1FL)
    override val onSurface = Color(0xFFE6E1E5L)
    override val surfaceVariant = Color(0xFF49454FL)
    override val onSurfaceVariant = Color(0xFFCAC4D0L)
    override val surfaceTint = primary
        
    override val surfaceContainerLowest = Color(0xFF0F0D10L)
    override val surfaceContainerLow = Color(0xFF1D1B20L)
    override val surfaceContainer = Color(0xFF211F26L)
    override val surfaceContainerHigh = Color(0xFF2B2930L)
    override val surfaceContainerHighest = Color(0xFF36343CL)
        
    override val inverseSurface = Color(0xFFE6E1E5L)
    override val inverseOnSurface = Color(0xFF313033L)
    override val inversePrimary = Color(0xFF6750A4L)
        
    override val error = Color(0xFFF2B8B5L)
    override val onError = Color(0xFF601410L)
    override val errorContainer = Color(0xFF8C1D18L)
    override val onErrorContainer = Color(0xFFF9DECCL)
        
    override val outline = Color(0xFF938F99L)
    override val outlineVariant = Color(0xFF49454FL)
    override val scrim = Color(0xFF000000L)
        
        // ==================== 扩展色板（ExtendColors 精选·暗色） ====================
    override val red = Color(0xFFF76560L)
    override val orange = Color(0xFFFF9A2EL)
    override val gold = Color(0xFFFBE35AL)
    override val lime = Color(0xFFBCE855L)
    override val green = Color(0xFF27C346L)
    override val cyan = Color(0xFF3AC9E4L)
    override val blue = Color(0xFF4080FFL)
    override val arcoblue = Color(0xFF25A7F5L)
    override val purple = Color(0xFF8E51DAL)
    override val magenta = Color(0xFFF970B9L)
    override val gray = Color(0xFFA9A9B8L)
    }
    
    // ==================== 字体尺寸 token ====================
    
    object Typography {
        const val DISPLAY_LARGE = 57f
        const val DISPLAY_MEDIUM = 45f
        const val DISPLAY_SMALL = 36f
        const val HEADLINE_LARGE = 32f
        const val HEADLINE_MEDIUM = 28f
        const val HEADLINE_SMALL = 24f
        const val TITLE_LARGE = 22f
        const val TITLE_MEDIUM = 16f
        const val TITLE_SMALL = 14f
        const val BODY_LARGE = 16f
        const val BODY_MEDIUM = 14f
        const val BODY_SMALL = 12f
        const val LABEL_LARGE = 14f
        const val LABEL_MEDIUM = 12f
        const val LABEL_SMALL = 11f
    }
    
    // ==================== 间距 token ====================
    
    object Spacing {
        const val XS = 2f
        const val SM = 4f
        const val MD = 8f
        const val BASE = 12f
        const val LG = 16f
        const val XL = 20f
        const val XXL = 24f
        const val XXXL = 32f
    }
    
    // ==================== 圆角 token ====================
    
    object Radius {
        const val SM = 4f
        const val MD = 8f
        const val LG = 12f
        const val XL = 16f
        const val XXL = 24f
        const val PILL = 9999f
    }
}
