package com.mhss.app.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.ui.Res
import com.mhss.app.ui.cancel
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.delete_bookmark
import com.mhss.app.ui.delete_bookmark_confirmation_message
import com.mhss.app.ui.delete_bookmark_confirmation_title
import com.mhss.app.ui.description
import com.mhss.app.ui.ic_delete
import com.mhss.app.ui.ic_open_link
import com.mhss.app.ui.invalid_url
import com.mhss.app.ui.open_link
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.title
import com.mhss.app.ui.url
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun BookmarkDetailsScreen(
    navController: NavHostController,
    bookmarkId: String?,
    viewModel: BookmarkDetailsViewModel = koinViewModel(parameters = { parametersOf(bookmarkId.orEmpty()) }),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val state = viewModel.bookmarkDetailsUiState
    val snackbarHostState = state.snackbarHostState
    var openDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf(state.bookmark?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(state.bookmark?.description ?: "") }
    var url by rememberSaveable { mutableStateOf(state.bookmark?.url ?: "") }

    LaunchedEffect(state.bookmark) {
        if (state.bookmark != null) {
            title = state.bookmark.title
            description = state.bookmark.description
            url = state.bookmark.url
        }
    }
    LaunchedEffect(state.navigateUp) {
        if (state.navigateUp) {
            openDialog = false
            navController.navigateUp()
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(
                BookmarkDetailsEvent.ScreenOnStop(
                    Bookmark(
                        title = title,
                        description = description,
                        url = url,
                        id = bookmarkId ?: Uuid.generateV7().toString()
                    )
                )
            )
        }
    }
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
                    if (state.bookmark != null) IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.delete_bookmark)
                        )
                    }
                    IconButton(onClick = {
                        if (url.isValidUrl()) {
                            uriHandler.openUri(if (!url.startsWith("https://") && !url.startsWith("http://")) "http://$url" else url)
                        } else scope.launch {
                            snackbarHostState.showSnackbar(Res.string.invalid_url)
                        }
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_open_link),
                            contentDescription = stringResource(Res.string.open_link),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(text = stringResource(Res.string.url)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(Res.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = stringResource(Res.string.description)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (openDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDialog = false },
                title = { Text(stringResource(Res.string.delete_bookmark_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            Res.string.delete_bookmark_confirmation_message
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(BookmarkDetailsEvent.DeleteBookmark(state.bookmark!!))
                        },
                    ) {
                        Text(stringResource(Res.string.delete_bookmark), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDialog = false
                        }) {
                        Text(stringResource(Res.string.cancel), color = Color.White)
                    }
                }
            )
    }
}