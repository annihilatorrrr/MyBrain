package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.mybrain.presentation.main.components.SpaceCard
import com.mhss.app.presentation.components.drawAiGradientRadials
import com.mhss.app.ui.Res
import com.mhss.app.ui.ai_chat_img
import com.mhss.app.ui.assistant
import com.mhss.app.ui.bookmarks
import com.mhss.app.ui.bookmarks_img
import com.mhss.app.ui.calendar
import com.mhss.app.ui.calendar_img
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.singleGradientBackground
import com.mhss.app.ui.diary
import com.mhss.app.ui.diary_img
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.notes
import com.mhss.app.ui.notes_img
import com.mhss.app.ui.spaces
import com.mhss.app.ui.tasks
import com.mhss.app.ui.tasks_img
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Purple
import com.mhss.app.ui.theme.Red
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun SpacesScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            MyBrainAppBar(stringResource(Res.string.spaces))
        }
    ) { paddingValues ->
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(
                top = 10.dp,
                bottom = 32.dp,
                start = 10.dp,
                end = 10.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(spaces) { (title, image, color, screen) ->
                SpaceCard(
                    title = stringResource(title),
                    image = image,
                    onClick = {
                        navController.navigate(screen)
                    },
                    contentModifier = Modifier.singleGradientBackground(
                        gradientColor = color,
                        background = MaterialTheme.colorScheme.surfaceVariant,
                        backgroundAlpha = 0.35f,
                    )
                )
            }
            item {
                SpaceCard(
                    modifier = Modifier,
                    title = stringResource(Res.string.assistant),
                    image = Res.drawable.ai_chat_img,
                    onClick = {
                        navController.navigate(Screen.AssistantScreen)
                    },
                    contentModifier = Modifier
                        .drawBehind {
                            drawAiGradientRadials(
                                background = surfaceVariant,
                                backgroundAlpha = 0.4f
                            )
                        }
                )

            }
        }
    }
}


private val spaces = listOf(
    Space(Res.string.notes, Res.drawable.notes_img, Blue, Screen.NotesScreen),
    Space(Res.string.tasks, Res.drawable.tasks_img, Red, Screen.TasksScreen()),
    Space(Res.string.diary, Res.drawable.diary_img, Green, Screen.DiaryScreen),
    Space(Res.string.bookmarks, Res.drawable.bookmarks_img, Orange, Screen.BookmarksScreen),
    Space(Res.string.calendar, Res.drawable.calendar_img, Purple, Screen.CalendarScreen),
)

private data class Space(
    val title: StringResource,
    val image: DrawableResource,
    val color: Color,
    val route: Screen
)

@Preview(widthDp = 360, heightDp = 680)
@Composable
fun SpacesScreenPreview() {
    BasePreview {
        SpacesScreen(navController = rememberNavController())
    }
}

@Preview(widthDp = 360, heightDp = 680)
@Composable
fun SpacesScreenPreviewDark() {
    BasePreview(darkTheme = true) {
        SpacesScreen(navController = rememberNavController())
    }
}