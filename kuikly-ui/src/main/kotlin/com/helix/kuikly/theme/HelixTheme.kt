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

    // ==================== Material3 核心色彩（亮色模式） ====================
    
    object Light {
        val primary = Color(0xFF6750A4L)
        val onPrimary = Color(0xFFFFFFFFL)
        val primaryContainer = Color(0xFFEADDFFL)
        val onPrimaryContainer = Color(0xFF21005DL)
        
        val secondary = Color(0xFF625B71L)
        val onSecondary = Color(0xFFFFFFFFL)
        val secondaryContainer = Color(0xFFE8DEF8L)
        val onSecondaryContainer = Color(0xFF1D192BL)
        
        val tertiary = Color(0xFF7D5260L)
        val onTertiary = Color(0xFFFFFFFFL)
        val tertiaryContainer = Color(0xFFFFD8E4L)
        val onTertiaryContainer = Color(0xFF31111DL)
        
        val background = Color(0xFFFFFBFEL)
        val onBackground = Color(0xFF1C1B1FL)
        
        val surface = Color(0xFFFFFBFEL)
        val onSurface = Color(0xFF1C1B1FL)
        val surfaceVariant = Color(0xFFE7E0ECL)
        val onSurfaceVariant = Color(0xFF49454FL)
        val surfaceTint = primary
        
        val surfaceContainerLowest = Color(0xFFFFFFFFL)
        val surfaceContainerLow = Color(0xFFF7F2FAL)
        val surfaceContainer = Color(0xFFF3EDF7L)
        val surfaceContainerHigh = Color(0xFFECE6F0L)
        val surfaceContainerHighest = Color(0xFFE6E0E9L)
        
        val inverseSurface = Color(0xFF313033L)
        val inverseOnSurface = Color(0xFFF4EFF4L)
        val inversePrimary = Color(0xFFD0BCFFL)
        
        val error = Color(0xFFB3261EL)
        val onError = Color(0xFFFFFFFFL)
        val errorContainer = Color(0xFFF9DECCL)
        val onErrorContainer = Color(0xFF410E0BL)
        
        val outline = Color(0xFF79747EL)
        val outlineVariant = Color(0xFFCAC4D0L)
        val scrim = Color(0xFF000000L)
        
        // ==================== 扩展色板（ExtendColors 精选） ====================
        val red = Color(0xFFF53F3FL)
        val orange = Color(0xFFFA8C16L)
        val gold = Color(0xFFFADC60L)
        val lime = Color(0xFFA8E10CL)
        val green = Color(0xFF00B42AL)
        val cyan = Color(0xFF14C9C9L)
        val blue = Color(0xFF165DFFL)
        val arcoblue = Color(0xFF00A3F0L)
        val purple = Color(0xFF722ED1L)
        val magenta = Color(0xFFF77AB0L)
        val gray = Color(0xFF86909CL)
    }
    
    // ==================== Material3 核心色彩（暗色模式） ====================
    
    object Dark {
        val primary = Color(0xFFD0BCFFL)
        val onPrimary = Color(0xFF381E72L)
        val primaryContainer = Color(0xFF4F378BL)
        val onPrimaryContainer = Color(0xFFEADDFFL)
        
        val secondary = Color(0xFFCCC2DCL)
        val onSecondary = Color(0xFF332D41L)
        val secondaryContainer = Color(0xFF4A4458L)
        val onSecondaryContainer = Color(0xFFE8DEF8L)
        
        val tertiary = Color(0xFFEFB8C8L)
        val onTertiary = Color(0xFF492532L)
        val tertiaryContainer = Color(0xFF633B48L)
        val onTertiaryContainer = Color(0xFFFFD8E4L)
        
        val background = Color(0xFF1C1B1FL)
        val onBackground = Color(0xFFE6E1E5L)
        
        val surface = Color(0xFF1C1B1FL)
        val onSurface = Color(0xFFE6E1E5L)
        val surfaceVariant = Color(0xFF49454FL)
        val onSurfaceVariant = Color(0xFFCAC4D0L)
        val surfaceTint = primary
        
        val surfaceContainerLowest = Color(0xFF0F0D10L)
        val surfaceContainerLow = Color(0xFF1D1B20L)
        val surfaceContainer = Color(0xFF211F26L)
        val surfaceContainerHigh = Color(0xFF2B2930L)
        val surfaceContainerHighest = Color(0xFF36343CL)
        
        val inverseSurface = Color(0xFFE6E1E5L)
        val inverseOnSurface = Color(0xFF313033L)
        val inversePrimary = Color(0xFF6750A4L)
        
        val error = Color(0xFFF2B8B5L)
        val onError = Color(0xFF601410L)
        val errorContainer = Color(0xFF8C1D18L)
        val onErrorContainer = Color(0xFFF9DECCL)
        
        val outline = Color(0xFF938F99L)
        val outlineVariant = Color(0xFF49454FL)
        val scrim = Color(0xFF000000L)
        
        // ==================== 扩展色板（ExtendColors 精选·暗色） ====================
        val red = Color(0xFFF76560L)
        val orange = Color(0xFFFF9A2EL)
        val gold = Color(0xFFFBE35AL)
        val lime = Color(0xFFBCE855L)
        val green = Color(0xFF27C346L)
        val cyan = Color(0xFF3AC9E4L)
        val blue = Color(0xFF4080FFL)
        val arcoblue = Color(0xFF25A7F5L)
        val purple = Color(0xFF8E51DAL)
        val magenta = Color(0xFFF970B9L)
        val gray = Color(0xFFA9A9B8L)
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
