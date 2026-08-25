package com.helix.app.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import com.helix.app.data.datastore.Settings

val LocalSettings = staticCompositionLocalOf<Settings> {
    error("No SettingsStore provided")
}
