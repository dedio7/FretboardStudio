package com.example.triadguitar.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.triadguitar.model.UiStrings
import com.example.triadguitar.theme.TriadGuitarTheme
import com.example.triadguitar.ui.TriadUiState
import com.example.triadguitar.ui.TriadViewModel
import com.example.triadguitar.ui.components.SettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: TriadViewModel = viewModel(),
) {
    val state: TriadUiState = viewModel.uiState.collectAsStateWithLifecycle().value

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
                            Text(UiStrings(state.language).settingsButton, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize(),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ReferenceVisualizerContent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
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
