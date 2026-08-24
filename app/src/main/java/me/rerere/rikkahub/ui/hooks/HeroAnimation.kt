package me.rerere.rikkahub.ui.hooks

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import me.rerere.rikkahub.ui.context.LocalSharedTransitionScope

// navigation2 迁移：nav3 的 LocalNavAnimatedContentScope 在 nav2 中不存在，
// 由应用自行定义 CompositionLocal，并在 NavGraphBuilder 条目内（RouteActivity 的 screen 包装函数）注入
val LocalNavAnimatedContentScope = compositionLocalOf<AnimatedVisibilityScope> {
    error("No AnimatedVisibilityScope provided")
}

@Composable
fun Modifier.heroAnimation(
    key: Any,
): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current
    return with(sharedTransitionScope) {
        this@heroAnimation.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}
