package com.example.triadguitar.ui.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triadguitar.model.ChordCategory
import com.example.triadguitar.model.ChordDictionaryRepository
import com.example.triadguitar.model.NoteNotation
import com.example.triadguitar.model.Pitch
import com.example.triadguitar.ui.components.ChordDiagramView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordDictionaryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    notation: NoteNotation = NoteNotation.ITALIAN
) {
    var selectedRoot by remember { mutableStateOf(Pitch(0)) }
    var selectedCategory by remember { mutableStateOf(ChordCategory.TRIADS) }
    var selectedType by remember { mutableStateOf(ChordDictionaryRepository.ALL_TYPES.first()) }

    val filteredTypes = remember(selectedCategory) {
        ChordDictionaryRepository.ALL_TYPES.filter { it.category == selectedCategory }
    }

    val currentDiagrams = remember(selectedRoot, selectedType, notation) {
        ChordDictionaryRepository.getDiagramsForChord(selectedRoot, selectedType, notation)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "📖 PRONTUARIO ACCORDI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("➔", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                            .clickable {
                                selectedRoot = Pitch.ALL.random()
                                selectedType = ChordDictionaryRepository.ALL_TYPES.random()
                                selectedCategory = selectedType.category
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎲 SORPRESA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Root Note Pitch Selector
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = "TONICA // ROOT NOTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Pitch.ALL.forEach { pitch ->
                        val isSelected = pitch == selectedRoot
                        val name = pitch.getDisplayName(notation)

                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                    else Color(0xFF10121C)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color(0xFF222638),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedRoot = pitch },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else Color(0xFFB0B8C8),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChordCategory.entries.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else Color(0xFF12141F)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF262C44),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedCategory = cat
                                selectedType = ChordDictionaryRepository.ALL_TYPES.first { it.category == cat }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF8F9BB3),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // 3. Chord Types Flow Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredTypes.forEach { type ->
                    val isSelected = type.id == selectedType.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                                else Color(0xFF10121C)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color(0xFF222638),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${type.name} (${type.symbol})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF8F9BB3),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // 4. Formula & Description Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131622))
                    .border(1.dp, Color(0xFF22273D), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "${selectedRoot.getDisplayName(notation)} ${selectedType.name} [${selectedType.symbol}]",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedType.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 5. Diagrams Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentDiagrams.forEach { diag ->
                    ChordDiagramView(diagram = diag)
                }
            }
        }
    }
}
