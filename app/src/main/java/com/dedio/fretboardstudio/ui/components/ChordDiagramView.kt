package com.dedio.fretboardstudio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.fretboardstudio.model.ChordDiagram

@Composable
fun ChordDiagramView(
    diagram: ChordDiagram,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F121C))
            .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = diagram.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 15.sp
                )
            )

            Text(
                text = diagram.positionLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val w = size.width
                val h = size.height

                val topMargin = 20f
                val leftMargin = 25f
                val rightMargin = w - 15f
                val gridWidth = rightMargin - leftMargin
                val gridHeight = h - topMargin - 10f

                val stringSpacing = gridWidth / 5f
                val fretSpacing = gridHeight / 5f

                if (diagram.baseFret == 1) {
                    drawRect(
                        color = Color.LightGray,
                        topLeft = Offset(leftMargin, topMargin - 4f),
                        size = Size(gridWidth, 5f)
                    )
                }

                for (f in 0..5) {
                    val y = topMargin + f * fretSpacing
                    drawLine(
                        color = Color(0xFF2E354E),
                        start = Offset(leftMargin, y),
                        end = Offset(rightMargin, y),
                        strokeWidth = 2f
                    )
                }

                for (s in 0..5) {
                    val x = leftMargin + s * stringSpacing
                    drawLine(
                        color = Color(0xFF535D7E),
                        start = Offset(x, topMargin),
                        end = Offset(x, topMargin + gridHeight),
                        strokeWidth = 1.5f + (5 - s) * 0.3f
                    )
                }

                diagram.stringFrets.forEachIndexed { sIdx, fretVal ->
                    val x = leftMargin + sIdx * stringSpacing
                    if (fretVal == 0) {
                        drawCircle(
                            color = accentColor,
                            radius = 4f,
                            center = Offset(x, topMargin / 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                        )
                    } else if (fretVal == -1) {
                        val crossSize = 3f
                        val yCenter = topMargin / 2f
                        drawLine(Color.Gray, Offset(x - crossSize, yCenter - crossSize), Offset(x + crossSize, yCenter + crossSize), strokeWidth = 1.5f)
                        drawLine(Color.Gray, Offset(x - crossSize, yCenter + crossSize), Offset(x + crossSize, yCenter - crossSize), strokeWidth = 1.5f)
                    }
                }

                diagram.stringFrets.forEachIndexed { sIdx, fretVal ->
                    if (fretVal > 0) {
                        val relFret = if (diagram.baseFret > 1) (fretVal - diagram.baseFret + 1) else fretVal
                        if (relFret in 1..5) {
                            val x = leftMargin + sIdx * stringSpacing
                            val y = topMargin + (relFret - 0.5f) * fretSpacing

                            drawCircle(
                                color = accentColor,
                                radius = 9f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}
