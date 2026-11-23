package com.dedio.fretboardstudio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                            Text(UiStrings(state.language).settings, color = MaterialTheme.colorScheme.primary)
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
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Overlay: the fretboard keeps its full height.
                MetronomeBar(
                    viewModel = metronomeViewModel,
                    language = state.language,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                )
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
