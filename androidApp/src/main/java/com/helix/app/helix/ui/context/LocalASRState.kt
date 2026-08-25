package com.helix.app.ui.context

import androidx.compose.runtime.compositionLocalOf
import com.helix.app.ui.hooks.CustomAsrState

val LocalASRState = compositionLocalOf<CustomAsrState> { error("Not provided yet") }

