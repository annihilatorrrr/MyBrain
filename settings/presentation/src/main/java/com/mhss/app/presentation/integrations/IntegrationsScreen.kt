package com.mhss.app.presentation.integrations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.AiConstants
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.presentation.integrations.components.AiProviderCard
import com.mhss.app.presentation.integrations.components.CustomURLSection
import com.mhss.app.presentation.integrations.components.ExternalNotesCard
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.MyBrainAppBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun IntegrationsScreen(
    viewModel: IntegrationsViewModel = koinViewModel()
) {

    Scaffold(
        topBar = {
            MyBrainAppBar(
                title = stringResource(R.string.integrations)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = paddingValues) {
            item {
                val provider by viewModel.getAiProvider().collectAsStateWithLifecycle(AiProvider.None)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(25.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.ai),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Switch(
                                checked = provider != AiProvider.None,
                                onCheckedChange = {
                                    viewModel.onEvent(IntegrationsEvent.ToggleAiProvider(it))
                                }
                            )
                        }
                        AnimatedVisibility(provider != AiProvider.None) {
                            val geminiKey by viewModel.getSettings(
                                    stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                            ""
                            ).collectAsStateWithLifecycle("")
                            val geminiModel by viewModel.getSettings(
                                stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                                AiConstants.GEMINI_DEFAULT_MODEL
                            ).collectAsStateWithLifecycle("")
                            val openaiKey by viewModel.getSettings(
                                stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                                ""
                            ).collectAsStateWithLifecycle("")
                            val openaiModel by viewModel.getSettings(
                                stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                                AiConstants.OPENAI_DEFAULT_MODEL
                            ).collectAsStateWithLifecycle("")
                            val openaiUseCustomURL by viewModel.getSettings(
                                booleanPreferencesKey(PrefsConstants.OPENAI_USE_URL_KEY),
                                false
                            ).collectAsStateWithLifecycle(false)
                            val openaiCustomURL by viewModel.getSettings(
                                stringPreferencesKey(PrefsConstants.OPENAI_URL_KEY),
                                AiConstants.OPENAI_BASE_URL
                            ).collectAsStateWithLifecycle("")
                            Column {
                                Spacer(Modifier.height(8.dp))
                                AiProviderCard(
                                    name = stringResource(R.string.gemini),
                                    description = stringResource(R.string.gemini_description),
                                    selected = provider == AiProvider.Gemini,
                                    key = geminiKey,
                                    model = geminiModel,
                                    keyInfoURL = AiConstants.GEMINI_KEY_INFO_URL,
                                    modelInfoURL = AiConstants.GEMINI_MODELS_INFO_URL,
                                    onKeyChange = {
                                        viewModel.onEvent(IntegrationsEvent.UpdateGeminiKey(it))
                                    },
                                    onModelChange = {
                                        viewModel.onEvent(IntegrationsEvent.UpdateGeminiModel(it))
                                    },
                                    onClick = {
                                        viewModel.onEvent(
                                            IntegrationsEvent.SelectProvider(
                                                AiProvider.Gemini
                                            )
                                        )
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                                AiProviderCard(
                                    name = stringResource(R.string.openai),
                                    description = stringResource(R.string.openai_description),
                                    selected = provider == AiProvider.OpenAI,
                                    key = openaiKey,
                                    model = openaiModel,
                                    keyInfoURL = AiConstants.OPENAI_KEY_INFO_URL,
                                    modelInfoURL = AiConstants.OPENAI_MODELS_INFO_URL,
                                    onKeyChange = {
                                        viewModel.onEvent(IntegrationsEvent.UpdateOpenAiKey(it))
                                    },
                                    onModelChange = {
                                        viewModel.onEvent(IntegrationsEvent.UpdateOpenAiModel(it))
                                    },
                                    onClick = {
                                        viewModel.onEvent(
                                            IntegrationsEvent.SelectProvider(
                                                AiProvider.OpenAI
                                            )
                                        )
                                    }
                                ) {
                                    CustomURLSection(
                                        enabled = openaiUseCustomURL,
                                        url = openaiCustomURL,
                                        onSave = {
                                            viewModel.onEvent(
                                                IntegrationsEvent.UpdateOpenAiCustomURL(
                                                    it
                                                )
                                            )
                                        },
                                        onEnable = {
                                            viewModel.onEvent(
                                                IntegrationsEvent.ToggleOpenAiCustomURL(
                                                    it
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                val isExternalNotesEnabled by viewModel.getSettings(
                    booleanPreferencesKey(PrefsConstants.EXTERNAL_NOTES_ENABLED),
                    false
                ).collectAsStateWithLifecycle(false)
                val selectedFolder by viewModel.getExternalNotesFolderPath()
                    .collectAsStateWithLifecycle(null)
                ExternalNotesCard(
                    isEnabled = isExternalNotesEnabled,
                    selectedFolder = selectedFolder,
                    onSwitchToggled = {
                        viewModel.onEvent(IntegrationsEvent.SetExternalNotesEnabled(it))
                    },
                    onFolderSelected = {
                        viewModel.onEvent(IntegrationsEvent.SelectExternalNotesFolder(it))
                    }
                )
            }
        }
    }
}