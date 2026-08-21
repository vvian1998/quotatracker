package com.quotatracker.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotatracker.app.ui.theme.AmberAccent
import com.quotatracker.app.ui.theme.CardBackgroundElevated
import com.quotatracker.app.ui.theme.RedWarning
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TealVariant
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter

@Composable
fun QuotaGauge(
    usedBytes: Long,
    totalQuotaBytes: Long,
    subtitle: String = "dari kuota bulan ini",
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 16.dp
) {
    val percentage = DataFormatter.calculatePercentage(usedBytes, totalQuotaBytes)

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "gauge_anim"
    )

    val (usedVal, usedUnit) = DataFormatter.formatBytesParts(usedBytes)
    val formattedTotal = DataFormatter.formatBytes(totalQuotaBytes)

    val gaugeColor = when {
        percentage >= 0.9f -> RedWarning
        percentage >= 0.75f -> AmberAccent
        else -> TealPrimary
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val canvasSize = this.size.minDimension
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(canvasSize, canvasSize)
            val topLeft = Offset((this.size.width - canvasSize) / 2, (this.size.height - canvasSize) / 2)

            // Background full track circle
            drawArc(
                color = CardBackgroundElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Foreground progress arc with gradient
            if (animatedPercentage > 0f) {
                val sweep = animatedPercentage * 360f
                val brush = Brush.sweepGradient(
                    colors = listOf(
                        TealPrimary,
                        if (percentage > 0.6f) AmberAccent else TealVariant,
                        if (percentage > 0.85f) RedWarning else AmberAccent
                    )
                )

                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "${DataFormatter.formatBytes(usedBytes)} / $formattedTotal",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = usedVal,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 44.sp,
                    lineHeight = 48.sp
                )
                Text(
                    text = " $usedUnit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
