package com.mhss.app.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.defaultMarkdownTypography
import com.mhss.app.ui.gradientBrushColor
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.SecondaryColor
import com.mhss.app.util.date.formatTime
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor


@Composable
fun LazyItemScope.MessageCard(
    message: AiMessage,
    onCopy: (String) -> Unit,
) {
    when (message) {
        is AiMessage.UserMessage -> UserMessageCard(message = message, onCopy = onCopy)
        is AiMessage.AssistantMessage -> AssistantMessageCard(message = message, onCopy = onCopy)
        is AiMessage.ToolCall -> ToolCallCard(toolCall = message)
    }
}

@Composable
private fun LazyItemScope.UserMessageCard(
    message: AiMessage.UserMessage,
    onCopy: (String) -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp, start = 48.dp, bottom = 4.dp, top = 8.dp)
            .animateItem(
                fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            )
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 4.dp,
                bottomStart = 24.dp,
                bottomEnd = 14.dp
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = { showContextMenu = true }
        ) {
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            gradientBrushColor(
                                0f to SecondaryColor,
                                1f to surfaceVariant
                                    .copy(alpha = 0.3f)
                                    .compositeOver(SecondaryColor)
                            ), Offset.Zero, size,
                            size.minDimension * 1.5f
                        )
                    }
            ) {
                Markdown(
                    content = message.content,
                    modifier = Modifier.padding(top = 7.dp, start = 12.dp, end = 8.dp),
                    imageTransformer = Coil2ImageTransformerImpl,
                    colors = markdownColor(text = Color.White),
                    typography = defaultMarkdownTypography()
                )
                if (message.attachments.isNotEmpty()) {
                    AiAttachmentsSection(
                        attachments = message.attachments,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Text(
                    text = message.time.formatTime(context),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.copy)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = stringResource(id = R.string.copy)
                            )
                        },
                        onClick = {
                            showContextMenu = false
                            onCopy(message.content)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.AssistantMessageCard(
    message: AiMessage.AssistantMessage,
    onCopy: (String) -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 48.dp, start = 8.dp, bottom = 4.dp, top = 8.dp)
            .animateItem(
                fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            )
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 20.dp,
                bottomStart = 14.dp,
                bottomEnd = 20.dp
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = { showContextMenu = true }
        ) {
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawAiGradientRadials(surfaceVariant, radius = size.minDimension * 1.2f)
                    }
            ) {
                Markdown(
                    content = message.content,
                    modifier = Modifier.padding(top = 7.dp, start = 8.dp, end = 12.dp),
                    imageTransformer = Coil2ImageTransformerImpl,
                    colors = markdownColor(text = MaterialTheme.colorScheme.onSurfaceVariant),
                    typography = defaultMarkdownTypography()
                )
                Text(
                    text = message.time.formatTime(context),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.copy)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = stringResource(id = R.string.copy)
                            )
                        },
                        onClick = {
                            showContextMenu = false
                            onCopy(message.content)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.ToolCallCard(
    toolCall: AiMessage.ToolCall,
) {
    var expanded by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 48.dp, start = 8.dp, bottom = 4.dp, top = 8.dp)
            .animateItem(
                fadeInSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (toolCall.isFailed) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer
            ),
            onClick = { expanded = !expanded }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tools),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = toolCall.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(start = 8.dp),
                            textDecoration = if (toolCall.isFailed) TextDecoration.LineThrough else null
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = stringResource(R.string.tool_call_content),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = toolCall.rawContent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.tool_call_result),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = toolCall.resultRawContent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Text(
                    text = toolCall.time.formatTime(context),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview
@Composable
fun MessageCardPreview() {
    MyBrainTheme {
        val demoText = remember {
            LoremIpsum(60).values.first()
        }
        LazyColumn {
            item {
                MessageCard(
                    message = AiMessage.UserMessage(
                        content = demoText,
                        time = 1111111111,
                        uuid = "123",
                        attachments = listOf(
                            AiMessageAttachment.Note(
                                Note(
                                    "This is a test tile for the note",
                                    "Description",
                                    1111111111,
                                    id = "1"
                                )
                            ),
                            AiMessageAttachment.CalenderEvents,
                        )
                    ),
                    onCopy = {}
                )
            }
            item {
                MessageCard(
                    message = AiMessage.AssistantMessage(
                        content = demoText,
                        time = 1111111112,
                        uuid = "567"
                    ),
                    onCopy = {}
                )
            }
            item {
                MessageCard(
                    message = AiMessage.ToolCall(
                        id = "test-id",
                        name = "searchNotes",
                        rawContent = "{\"query\": \"meeting notes\"}",
                        resultRawContent = "[{\"id\": 1, \"title\": \"Meeting Notes\", \"content\": \"Discussion about project...\"}]",
                        time = 1111111113,
                        uuid = "890"
                    ),
                    onCopy = {}
                )
            }
        }
    }
}