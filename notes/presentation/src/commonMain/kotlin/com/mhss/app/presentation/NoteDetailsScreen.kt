@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.presentation.components.AiResultSheet
import com.mhss.app.presentation.components.GradientIconButton
import com.mhss.app.presentation.components.ShareNoteAsPlainTextOption
import com.mhss.app.ui.Res
import com.mhss.app.ui.auto_format
import com.mhss.app.ui.cancel
import com.mhss.app.ui.change_folder
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.defaultMarkdownTypography
import com.mhss.app.ui.components.common.withHardLineBreaks
import com.mhss.app.ui.correct_spelling
import com.mhss.app.ui.delete_note
import com.mhss.app.ui.delete_note_confirmation_message
import com.mhss.app.ui.delete_note_confirmation_title
import com.mhss.app.ui.delete_task
import com.mhss.app.ui.folders
import com.mhss.app.ui.ic_auto_format
import com.mhss.app.ui.ic_create_folder
import com.mhss.app.ui.ic_delete
import com.mhss.app.ui.ic_folder
import com.mhss.app.ui.ic_pin
import com.mhss.app.ui.ic_pin_filled
import com.mhss.app.ui.ic_read_mode
import com.mhss.app.ui.ic_share
import com.mhss.app.ui.ic_spelling
import com.mhss.app.ui.ic_summarize
import com.mhss.app.ui.none
import com.mhss.app.ui.note_content
import com.mhss.app.ui.pin_note
import com.mhss.app.ui.reading_mode
import com.mhss.app.ui.share_note
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.summarize
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.title
import com.mhss.app.util.clipboard.copyText
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.foundation.text.selection.SelectionContainer

@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteId: String?,
    folderId: String?,
    viewModel: NoteDetailsViewModel = koinViewModel(
        parameters = { parametersOf(noteId.orEmpty(), folderId.orEmpty()) }
    ),
) {
    val state by viewModel.noteUiState.collectAsStateWithLifecycle()
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var openFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showShareMenu by rememberSaveable { mutableStateOf(false) }

    val formatter = LocalDateTimeFormatter.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboard.current

    val scope = rememberCoroutineScope()

    val title = viewModel.title
    val content = viewModel.content
    val pinned = state.pinned
    val readingMode = state.readingMode
    val folder = state.folder
    val lastModified by remember(state.note?.updatedDate) {
        derivedStateOf {
            state.note?.updatedDate?.let { formatter.formatDateDependingOnDay(it) } ?: ""
        }
    }
    var wordCountString by remember { mutableStateOf("") }
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val aiState = viewModel.aiState
    val showAiSheet = aiState.showAiSheet

    val liquidState = rememberLiquidState()
    LaunchedEffect(content) {
        delay(500)
        wordCountString = content.countWords().toString()
    }
    LaunchedEffect(state.navigateUp) {
        if (state.navigateUp) {
            openDeleteDialog = false
            navController.navigateUp()
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(NoteDetailsEvent.ScreenOnStop)
        }
    }
    Scaffold(
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
                    if (folder != null) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable { openFolderDialog = true }
                                .weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(Res.drawable.ic_folder),
                                stringResource(Res.string.folders),
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                                    .size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = folder.name,
                                modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        IconButton(onClick = { openFolderDialog = true }) {
                            Icon(
                                painterResource(Res.drawable.ic_create_folder),
                                stringResource(Res.string.folders),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(
                            painterResource(Res.drawable.ic_share),
                            stringResource(Res.string.share_note),
                            modifier = Modifier.size(18.dp),
                        )
                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false }
                        ) {
                            ShareNoteAsPlainTextOption(
                                title = title,
                                content = content,
                                onOptionSelected = { showShareMenu = false }
                            )
                        }
                    }
                    if (state.note != null) IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.delete_task),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdatePinned(!pinned))
                    }) {
                        Icon(
                            painter = if (pinned) painterResource(Res.drawable.ic_pin_filled)
                            else painterResource(Res.drawable.ic_pin),
                            contentDescription = stringResource(Res.string.pin_note),
                            modifier = Modifier.size(18.dp),
                            tint = Orange
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteDetailsEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_read_mode),
                            contentDescription = stringResource(Res.string.reading_mode),
                            modifier = Modifier.size(18.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                }
            )
        },
        snackbarHost = {
            LocalisedSnackbarHost(state.snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onEvent(NoteDetailsEvent.UpdateTitle(it)) },
                label = { Text(text = stringResource(Res.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(aiEnabled) {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        GradientIconButton(
                            text = stringResource(Res.string.summarize),
                            iconPainter = painterResource(Res.drawable.ic_summarize),
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.Summarize(content))
                            keyboardController?.hide()
                        }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(Res.string.auto_format),
                            iconPainter = painterResource(Res.drawable.ic_auto_format),
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.AutoFormat(content))
                            keyboardController?.hide()
                        }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(Res.string.correct_spelling),
                            iconPainter = painterResource(Res.drawable.ic_spelling),
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.CorrectSpelling(content))
                            keyboardController?.hide()
                        }
                    }
                }
            }
            if (readingMode)
                SelectionContainer {
                    Markdown(
                        content = content.withHardLineBreaks(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .padding(8.dp)
                            .liquefiable(liquidState),
                        imageTransformer = Coil2ImageTransformerImpl,
                        typography = defaultMarkdownTypography()
                    )
                }
            else
                OutlinedTextField(
                    value = content,
                    onValueChange = { viewModel.onEvent(NoteDetailsEvent.UpdateContent(it)) },
                    label = {
                        Text(text = stringResource(Res.string.note_content))
                    },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lastModified,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Text(
                    text = wordCountString,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }
        AnimatedVisibility(
            visible = showAiSheet,
            enter = slideInVertically(
                initialOffsetY = { it }, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            ),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    }, contentAlignment = Alignment.BottomCenter
            ) {
                AiResultSheet(
                    loading = aiState.loading,
                    result = aiState.result,
                    error = aiState.error?.toUserMessage(),
                    onCopyClick = {
                        scope.launch {
                            clipboard.copyText("ai result", aiState.result.toString())
                        }
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    },
                    onReplaceClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdateContent(aiState.result.toString()))
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    },
                    liquidState = liquidState,
                    onAddToNoteClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdateContent(aiState.result + "\n" + content))
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    }
                )
            }
        }
        if (openDeleteDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDeleteDialog = false },
                title = { Text(stringResource(Res.string.delete_note_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            Res.string.delete_note_confirmation_message,
                            state.note?.title!!
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(NoteDetailsEvent.DeleteNote(state.note!!))
                        },
                    ) {
                        Text(stringResource(Res.string.delete_note), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDeleteDialog = false
                        }) {
                        Text(stringResource(Res.string.cancel), color = Color.White)
                    }
                }
            )
        if (openFolderDialog) AlertDialog(
            onDismissRequest = { openFolderDialog = false },
            confirmButton = {},
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(stringResource(Res.string.change_folder))
                    FlowRow {
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable {
                                    viewModel.onEvent(NoteDetailsEvent.UpdateFolder(null))
                                    openFolderDialog = false
                                }
                                .background(if (folder == null) MaterialTheme.colorScheme.onBackground else Color.Transparent),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.none),
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (folder == null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        state.folders.forEach {
                            Row(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .clickable {
                                        viewModel.onEvent(NoteDetailsEvent.UpdateFolder(it))
                                        openFolderDialog = false
                                    }
                                    .background(if (folder?.id == it.id) MaterialTheme.colorScheme.onBackground else Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(Res.drawable.ic_folder),
                                    stringResource(Res.string.folders),
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    tint = if (folder?.id == it.id) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = it.name,
                                    modifier = Modifier.padding(
                                        end = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (folder?.id == it.id) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            })
    }
}

private fun String.countWords(): Int {
    var count = 0
    var inWord = false

    forEach { char ->
        if (char == ' ' || char == '\n') {
            inWord = false
        } else if (!inWord) {
            count++
            inWord = true
        }
    }

    return count
}
