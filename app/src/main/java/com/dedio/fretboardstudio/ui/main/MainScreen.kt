package com.dedio.fretboardstudio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dedio.fretboardstudio.audio.MetronomeViewModel
import com.dedio.fretboardstudio.model.UiStrings
import com.dedio.fretboardstudio.theme.TriadGuitarTheme
import com.dedio.fretboardstudio.ui.TriadViewModel
import com.dedio.fretboardstudio.ui.components.SettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: TriadViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val metronomeViewModel: MetronomeViewModel = viewModel()
    val metronomeState by metronomeViewModel.uiState.collectAsStateWithLifecycle()
    var activePanel by rememberSaveable { mutableStateOf(SidePanel.CONTROLS) }
    val strings = UiStrings(state.language)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Never keep a click loop running while the app is in the background.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) metronomeViewModel.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            metronomeViewModel.release()
        }
    }

    TriadGuitarTheme(visualTheme = state.visualTheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Fretboard Studio",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        )
                    },
                    actions = {
                        TextButton(onClick = { viewModel.openSettingsDialog(open = true) }) {
                            Text(strings.settings, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ReferenceVisualizerContent(
                        viewModel = viewModel,
                        activePanel = activePanel,
                        onActivePanelChange = { activePanel = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Keep the pill over the fretboard, not over an open side panel.
                val metronomeModifier = Modifier
                    .align(
                        when (activePanel) {
                            SidePanel.CHORDS -> Alignment.BottomStart
                            SidePanel.CONTROLS -> Alignment.BottomEnd
                            SidePanel.NONE -> Alignment.BottomCenter
                        },
                    )
                    .padding(
                        start = if (activePanel == SidePanel.CHORDS) 12.dp else 0.dp,
                        end = if (activePanel == SidePanel.CONTROLS) 12.dp else 0.dp,
                        bottom = 12.dp,
                    )

                if (metronomeState.isVisible) {
                    MetronomeBar(
                        viewModel = metronomeViewModel,
                        language = state.language,
                        modifier = metronomeModifier,
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        tonalElevation = 3.dp,
                        modifier = metronomeModifier
                            .clickable { metronomeViewModel.setVisible(visible = true) }
                            .semantics(mergeDescendants = true) {
                                contentDescription = strings.showMetronome
                                role = Role.Button
                            },
                    ) {
                        Text(
                            text = "♫ " + strings.metronome,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            if (state.isSettingsDialogOpen) {
                SettingsDialog(
                    currentNotation = state.notation,
                    currentLanguage = state.language,
                    currentTheme = state.visualTheme,
                    onNotationChange = viewModel::setNotation,
                    onLanguageChange = viewModel::setLanguage,
                    onThemeChange = viewModel::setVisualTheme
                ) { viewModel.openSettingsDialog(open = false) }
            }

        }
    }
}
