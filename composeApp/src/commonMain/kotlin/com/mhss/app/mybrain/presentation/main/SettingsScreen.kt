package com.mhss.app.mybrain.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.util.getAppVersion
import com.mhss.app.mybrain.util.supportsMaterialYouTheme
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.presentation.SettingsViewModel
import com.mhss.app.presentation.components.SettingsBasicLinkItem
import com.mhss.app.presentation.components.SettingsItemCard
import com.mhss.app.presentation.components.SettingsSwitchCard
import com.mhss.app.ui.AppFont
import com.mhss.app.ui.FirstDayOfWeekSettings
import com.mhss.app.ui.FontSizeSettings
import com.mhss.app.ui.Res
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.about
import com.mhss.app.ui.app_font
import com.mhss.app.ui.app_theme
import com.mhss.app.ui.app_version
import com.mhss.app.ui.assistant
import com.mhss.app.ui.auto_theme
import com.mhss.app.ui.block_screenshots
import com.mhss.app.ui.bookmarks
import com.mhss.app.ui.calendar
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.dark_theme
import com.mhss.app.ui.dashboard
import com.mhss.app.ui.diary
import com.mhss.app.ui.export_import
import com.mhss.app.ui.first_day_of_week
import com.mhss.app.ui.font_size
import com.mhss.app.ui.getFontSizeName
import com.mhss.app.ui.getName
import com.mhss.app.ui.ic_auto
import com.mhss.app.ui.ic_block_screenshot
import com.mhss.app.ui.ic_calendar
import com.mhss.app.ui.ic_code
import com.mhss.app.ui.ic_dark
import com.mhss.app.ui.ic_drop_down
import com.mhss.app.ui.ic_feature_issue
import com.mhss.app.ui.ic_font
import com.mhss.app.ui.ic_font_size
import com.mhss.app.ui.ic_github
import com.mhss.app.ui.ic_home
import com.mhss.app.ui.ic_import_export
import com.mhss.app.ui.ic_integrations
import com.mhss.app.ui.ic_lock
import com.mhss.app.ui.ic_paint_roller
import com.mhss.app.ui.ic_palette
import com.mhss.app.ui.ic_privacy
import com.mhss.app.ui.ic_roadmap
import com.mhss.app.ui.ic_sun
import com.mhss.app.ui.integrations
import com.mhss.app.ui.light_theme
import com.mhss.app.ui.lock_app
import com.mhss.app.ui.material_you
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_auth_method
import com.mhss.app.ui.notes
import com.mhss.app.ui.privacy_policy
import com.mhss.app.ui.product
import com.mhss.app.ui.project_on_github
import com.mhss.app.ui.project_roadmap
import com.mhss.app.ui.request_feature_report_bug
import com.mhss.app.ui.settings
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.spaces
import com.mhss.app.ui.start_up_screen
import com.mhss.app.ui.tasks
import com.mhss.app.ui.toAppFont
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@Composable
fun SettingsScreen(
    navController: NavHostController,
    appLockManager: AppLockManager,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val showMaterialYouOption = remember {
        supportsMaterialYouTheme()
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(stringResource(Res.string.settings))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val theme by viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                        ThemeSettings.AUTO.value
                    ).collectAsStateWithLifecycle(ThemeSettings.AUTO.value)
                ThemeSettingsItem(theme) {
                    when (theme) {
                        ThemeSettings.AUTO.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.LIGHT.value
                        )

                        ThemeSettings.LIGHT.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.DARK.value
                        )

                        ThemeSettings.DARK.value -> viewModel.saveSettings(
                            intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                            ThemeSettings.AUTO.value
                        )
                    }
                }
            }
            item {
                val screen by viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                        StartUpScreenSettings.SPACES.value
                    ).collectAsStateWithLifecycle(StartUpScreenSettings.SPACES.value)
                StartUpScreenSettingsItem(
                    screen
                ) { screenValue ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY),
                        screenValue
                    )
                }
            }
            item {
                val screen = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.APP_FONT_KEY),
                        AppFont.RUBIK.value
                    ).collectAsStateWithLifecycle(AppFont.RUBIK.value)
                AppFontSettingsItem(
                    screen.value,
                ) { font ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.APP_FONT_KEY),
                        font
                    )
                }
            }
            item {
                val fontSize = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.FONT_SIZE_KEY),
                        FontSizeSettings.NORMAL.value
                    ).collectAsStateWithLifecycle(FontSizeSettings.NORMAL.value)
                FontSizeSettingsItem(
                    fontSize.value,
                ) { fontSizeValue ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.FONT_SIZE_KEY),
                        fontSizeValue
                    )
                }
            }
            item {
                val firstDayOfWeek = viewModel
                    .getSettings(
                        intPreferencesKey(PrefsConstants.FIRST_DAY_OF_WEEK_KEY),
                        FirstDayOfWeekSettings.SUNDAY.value
                    ).collectAsStateWithLifecycle(FirstDayOfWeekSettings.SUNDAY.value)
                FirstDayOfWeekSettingsItem(
                    firstDayOfWeek.value,
                ) { value ->
                    viewModel.saveSettings(
                        intPreferencesKey(PrefsConstants.FIRST_DAY_OF_WEEK_KEY),
                        value
                    )
                }
            }
            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY),
                        false
                    ).collectAsStateWithLifecycle(false)
                SettingsSwitchCard(
                    text = stringResource(Res.string.block_screenshots),
                    checked = block.value,
                    painterResource(Res.drawable.ic_block_screenshot)
                ) {
                    viewModel.saveSettings(
                        booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY),
                        it
                    )
                }
            }

            item {
                val block = viewModel
                    .getSettings(
                        booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY),
                        false
                    ).collectAsStateWithLifecycle(false)
                SettingsSwitchCard(
                    text = stringResource(Res.string.lock_app),
                    checked = block.value,
                    iconPainter = painterResource(Res.drawable.ic_lock)
                ) {
                    if (appLockManager.canUseFeature()) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY),
                            it
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(Res.string.no_auth_method)
                        }
                    }
                }
            }


            if (showMaterialYouOption) {
                item {
                    val block = viewModel
                        .getSettings(
                            booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                            false
                        ).collectAsStateWithLifecycle(false)
                    SettingsSwitchCard(
                        text = stringResource(Res.string.material_you),
                        checked = block.value,
                        iconPainter = painterResource(Res.drawable.ic_palette)
                    ) {
                        viewModel.saveSettings(
                            booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                            it
                        )
                    }
                }
            }
            item {
                SettingsBasicLinkItem(
                    title = Res.string.integrations,
                    icon = Res.drawable.ic_integrations,
                    onClick = {
                        navController.navigate(Screen.IntegrationsScreen)
                    }
                )
            }
            item {
                SettingsBasicLinkItem(
                    title = Res.string.export_import,
                    icon = Res.drawable.ic_import_export,
                    onClick = {
                        navController.navigate(Screen.ImportExportScreen)
                    }
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.about),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = Res.string.app_version,
                    icon = Res.drawable.ic_code,
                    subtitle = getAppVersion(),
                    link = AboutLinks.GITHUB_RELEASES_LINK
                )
            }
            item {
                SettingsBasicLinkItem(
                    title = Res.string.project_on_github,
                    icon = Res.drawable.ic_github,
                    link = AboutLinks.PROJECT_GITHUB_LINK
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = Res.string.privacy_policy,
                    icon = Res.drawable.ic_privacy,
                    link = AboutLinks.PRIVACY_POLICY_LINK
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.product),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = Res.string.request_feature_report_bug,
                    icon = Res.drawable.ic_feature_issue,
                    link = AboutLinks.GITHUB_ISSUES_LINK
                )
            }

            item {
                SettingsBasicLinkItem(
                    title = Res.string.project_roadmap,
                    icon = Res.drawable.ic_roadmap,
                    link = AboutLinks.PROJECT_ROADMAP_LINK
                )
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun ThemeSettingsItem(theme: Int = 0, onClick: () -> Unit = {}) {
    SettingsItemCard(
        onClick = onClick,
        cornerRadius = 18.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_paint_roller),
                contentDescription = stringResource(Res.string.app_theme),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.app_theme),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        val themeTextId = remember(theme) {
            when (theme) {
                ThemeSettings.LIGHT.value -> Res.string.light_theme
                ThemeSettings.DARK.value -> Res.string.dark_theme
                else -> Res.string.auto_theme
            }
        }
        val themePainterId = remember(theme) {
            when (theme) {
                ThemeSettings.LIGHT.value -> Res.drawable.ic_sun
                ThemeSettings.DARK.value -> Res.drawable.ic_dark
                else -> Res.drawable.ic_auto
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(themeTextId, label = "themeTex") { id ->
                Text(
                    text = stringResource(id),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedContent(themePainterId, label = "themePainter") { id ->
                Icon(
                    painter = painterResource(id),
                    contentDescription = stringResource(themeTextId),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StartUpScreenSettingsItem(
    screen: Int,
    onScreenChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_home),
                contentDescription = stringResource(Res.string.start_up_screen),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.start_up_screen),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (screen) {
                        StartUpScreenSettings.SPACES.value -> stringResource(Res.string.spaces)
                        StartUpScreenSettings.DASHBOARD.value -> stringResource(Res.string.dashboard)
                        StartUpScreenSettings.NOTES.value -> stringResource(Res.string.notes)
                        StartUpScreenSettings.TASKS.value -> stringResource(Res.string.tasks)
                        StartUpScreenSettings.DIARY.value -> stringResource(Res.string.diary)
                        StartUpScreenSettings.BOOKMARKS.value -> stringResource(Res.string.bookmarks)
                        StartUpScreenSettings.CALENDAR.value -> stringResource(Res.string.calendar)
                        StartUpScreenSettings.ASSISTANT.value -> stringResource(Res.string.assistant)
                        else -> stringResource(Res.string.spaces)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(painter = painterResource(Res.drawable.ic_drop_down), contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                val options = listOf(
                    StartUpScreenSettings.SPACES to Res.string.spaces,
                    StartUpScreenSettings.DASHBOARD to Res.string.dashboard,
                    StartUpScreenSettings.NOTES to Res.string.notes,
                    StartUpScreenSettings.TASKS to Res.string.tasks,
                    StartUpScreenSettings.DIARY to Res.string.diary,
                    StartUpScreenSettings.BOOKMARKS to Res.string.bookmarks,
                    StartUpScreenSettings.CALENDAR to Res.string.calendar,
                    StartUpScreenSettings.ASSISTANT to Res.string.assistant
                )
                
                options.forEach { (screenOption, stringRes) ->
                    DropdownMenuItem(
                        onClick = {
                            onScreenChange(screenOption.value)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(stringRes),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppFontSettingsItem(
    selectedFont: Int,
    onFontChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val fonts = AppFont.entries
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_font),
                contentDescription = stringResource(Res.string.app_font),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.app_font),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedFont.toAppFont().getName(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(painter = painterResource(Res.drawable.ic_drop_down), contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                fonts.forEach {
                    DropdownMenuItem(onClick = {
                        onFontChange(it.value)
                        expanded = false
                    },
                        text = {
                            Text(
                                text = it.getName(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        })
                }
            }
        }
    }
}

@Composable
fun FontSizeSettingsItem(
    selectedFontSize: Int,
    onFontSizeChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val fontSizes = FontSizeSettings.entries
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_font_size),
                contentDescription = stringResource(Res.string.font_size),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.font_size),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedFontSize.getFontSizeName(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(painter = painterResource(Res.drawable.ic_drop_down), contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                fontSizes.forEach { fontSizeItem ->
                    DropdownMenuItem(
                        onClick = {
                            onFontSizeChange(fontSizeItem.value)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = cmpStringResource(fontSizeItem.title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FirstDayOfWeekSettingsItem(
    selectedDay: Int,
    onDayChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val options = FirstDayOfWeekSettings.entries
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            expanded = true
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = stringResource(Res.string.first_day_of_week),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.first_day_of_week),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cmpStringResource(FirstDayOfWeekSettings.fromValue(selectedDay).title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(painter = painterResource(Res.drawable.ic_drop_down), contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { dayOption ->
                    DropdownMenuItem(
                        onClick = {
                            onDayChange(dayOption.value)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = cmpStringResource(dayOption.title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}