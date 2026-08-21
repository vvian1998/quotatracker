package com.quotatracker.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotatracker.app.ui.theme.CardBackground
import com.quotatracker.app.ui.theme.CardBackgroundElevated
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.CardShape
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TealVariant
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter
import com.quotatracker.app.util.DateUtils

@Composable
fun WeeklyUsageChart(
    weeklyBreakdown: List<Pair<Long, Long>>, // List of Pair(timestamp, bytes)
    modifier: Modifier = Modifier
) {
    val maxBytes = (weeklyBreakdown.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(1024 * 1024L) // at least 1MB for scale

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas 7 Hari Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "Maks: ${DataFormatter.formatBytes(maxBytes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barCount = weeklyBreakdown.size.coerceAtLeast(1)
                    val barSpacing = width / barCount
                    val barWidth = barSpacing * 0.55f
                    val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

                    // Draw 3 horizontal guide grid lines
                    val gridColor = Color(0x1A4E657E)
                    for (i in 1..3) {
                        val y = height * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bars
                    weeklyBreakdown.forEachIndexed { index, pair ->
                        val bytes = pair.second
                        val ratio = (bytes.toFloat() / maxBytes.toFloat()).coerceIn(0.05f, 1f)
                        val barHeight = height * ratio * 0.9f
                        val x = index * barSpacing + (barSpacing - barWidth) / 2
                        val y = height - barHeight

                        // Background track
                        drawRoundRect(
                            color = CardBackgroundElevated.copy(alpha = 0.5f),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, height),
                            cornerRadius = cornerRadius
                        )

                        // Foreground bar fill
                        val brush = Brush.verticalGradient(
                            colors = listOf(TealVariant, TealPrimary),
                            startY = y,
                            endY = height
                        )

                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels below bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyBreakdown.forEachIndexed { index, pair ->
                    val isToday = index == weeklyBreakdown.size - 1
                    Text(
                        text = DateUtils.formatDayOfWeek(pair.first),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isToday) TealPrimary else TextSecondary,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
