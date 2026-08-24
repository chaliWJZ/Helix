package me.rerere.rikkahub.ui.context

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import me.rerere.rikkahub.Screen

// navigation2 迁移：Navigator 内部从 nav3 的 backStack 列表操作改为持有 nav2 的 NavHostController，
// 对外接口（navigate/clearAndNavigate/popBackStack + NavigateOptionsBuilder）保持不变，业务调用点零改动
class Navigator internal constructor(private val navController: NavHostController?) {
    // 无参构造：供离屏渲染等无真实导航场景使用（如聊天导出图片 ExportedChatImage），导航操作为空实现
    constructor() : this(null)

    fun navigate(screen: Screen, builder: NavigateOptionsBuilder.() -> Unit = {}) {
        val options = NavigateOptionsBuilder().apply(builder)
        val controller = navController ?: return
        controller.navigate(screen) {
            options.popUpToScreen?.let { target ->
                popUpTo(target) { inclusive = options.popUpToInclusive }
            }
            launchSingleTop = options.launchSingleTop
        }
    }

    fun clearAndNavigate(screen: Screen) {
        val controller = navController ?: return
        controller.navigate(screen) {
            popUpTo(controller.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun popBackStack() {
        navController?.popBackStack()
    }
}

class NavigateOptionsBuilder {
    internal var popUpToScreen: Screen? = null
    internal var popUpToInclusive: Boolean = false
    var launchSingleTop: Boolean = false

    fun popUpTo(screen: Screen, builder: PopUpToBuilder.() -> Unit = {}) {
        val options = PopUpToBuilder().apply(builder)
        popUpToScreen = screen
        popUpToInclusive = options.inclusive
    }
}

class PopUpToBuilder {
    var inclusive: Boolean = false
}

val LocalNavController = compositionLocalOf<Navigator> {
    error("No Navigator provided")
}
