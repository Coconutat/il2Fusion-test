package com.tools.il2fusion.app

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.tools.il2fusion.R

enum class AppTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Overview("overview", R.string.tab_overview, Icons.Outlined.Home),
    Mode("mode", R.string.tab_mode, Icons.Outlined.Tune),
    Parse("parse", R.string.tab_parse, Icons.Outlined.Description),
    Settings("settings", R.string.tab_settings, Icons.Outlined.Settings);

    companion object {
        fun fromRoute(route: String?): AppTab {
            return entries.firstOrNull { route?.startsWith(it.route) == true } ?: Overview
        }
    }
}
