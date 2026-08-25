package com.helix.app.ui.context

import androidx.compose.runtime.compositionLocalOf
import com.helix.app.ui.hooks.CustomTtsState

val LocalTTSState = compositionLocalOf<CustomTtsState> { error("Not provided yet") }
