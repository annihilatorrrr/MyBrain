package com.mhss.app.mybrain.presentation.main.components

import com.mhss.app.ui.Res
import com.mhss.app.ui.dashboard
import com.mhss.app.ui.ic_home
import com.mhss.app.ui.ic_home_filled
import com.mhss.app.ui.ic_settings
import com.mhss.app.ui.ic_settings_filled
import com.mhss.app.ui.ic_spaces
import com.mhss.app.ui.ic_spaces_filled
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.settings
import com.mhss.app.ui.spaces
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class BottomNavItem(
    val title: StringResource,
    val icon: DrawableResource,
    val iconSelected: DrawableResource,
    val screen: Screen
) {

    data object Dashboard : BottomNavItem(
        Res.string.dashboard, Res.drawable.ic_home, Res.drawable.ic_home_filled,
        Screen.DashboardScreen
    )
    data object Spaces : BottomNavItem(Res.string.spaces, Res.drawable.ic_spaces, Res.drawable.ic_spaces_filled,
        Screen.SpacesScreen
    )
    data object Settings: BottomNavItem(Res.string.settings, Res.drawable.ic_settings, Res.drawable.ic_settings_filled,
        Screen.SettingsScreen
    )

}