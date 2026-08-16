package com.mhss.app.widget.tasks

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.R
import com.mhss.app.widget.largeBackgroundBasedOnVersion
import com.mhss.app.widget.largeInnerBackgroundBasedOnVersion

@Composable
fun TasksHomeScreenWidget(
    tasks: List<Task>,
    backgroundOpacity: Float
) {
    val context = LocalContext.current
    val useSamsungWidgetBackground = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    val effectiveBackgroundOpacity = backgroundOpacity.coerceIn(1f / 255f, 254f / 255f)
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .largeBackgroundBasedOnVersion(
                backgroundOpacity = effectiveBackgroundOpacity,
                useHostCorners = useSamsungWidgetBackground
            )
            .appWidgetBackground()
    ) {
        Column(
            modifier = GlanceModifier.padding(8.dp)
        ) {
            Row(
                GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    context.getString(R.string.tasks),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp)
                        .clickable(actionRunCallback<NavigateToTasksAction>()),
                )
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable(actionRunCallback<NavigateToTasksAction>()),
                    horizontalAlignment = Alignment.End
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_add),
                        modifier = GlanceModifier
                            .size(22.dp)
                            .clickable(actionRunCallback<AddTaskAction>())
                        ,
                        contentDescription = "Add task",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                    )
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            LazyColumn(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
                    .largeInnerBackgroundBasedOnVersion(effectiveBackgroundOpacity),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    if (tasks.isEmpty()) {
                        item {
                            Text(
                                text = context.getString(R.string.no_tasks_message),
                                modifier = GlanceModifier.padding(16.dp),
                                style = TextStyle(
                                    color = GlanceTheme.colors.secondary,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        item { Spacer(GlanceModifier.height(6.dp)) }
                        items(tasks) { task ->
                            TaskWidgetItem(
                                task = task,
                                backgroundOpacity = effectiveBackgroundOpacity
                            )
                        }
                    }
            }
        }
    }
}
