package com.quotatracker.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.ui.theme.AmberAccent
import com.quotatracker.app.ui.theme.CardBackgroundElevated
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TealVariant
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter

@Composable
fun AppUsageCard(
    appUsage: AppDataUsage,
    maxUsageBytes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressRatio = if (maxUsageBytes > 0) {
        (appUsage.totalBytes.toFloat() / maxUsageBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Icon + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // App Icon
                if (appUsage.appIcon != null) {
                    val bitmap = appUsage.appIcon.toBitmap(width = 80, height = 80)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = appUsage.appName,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CardBackgroundElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = appUsage.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Mobile: ${DataFormatter.formatBytes(appUsage.mobileBytes)} • WiFi: ${DataFormatter.formatBytes(appUsage.wifiBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Usage amount
            Text(
                text = DataFormatter.formatBytes(appUsage.totalBytes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height
            val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

            // Background Track
            drawRoundRect(
                color = CardBackgroundElevated,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )

            // Foreground Fill
            if (animatedProgress > 0f) {
                val fillWidth = barWidth * animatedProgress
                val brush = Brush.horizontalGradient(
                    colors = listOf(TealPrimary, if (animatedProgress > 0.7f) AmberAccent else TealVariant),
                    startX = 0f,
                    endX = fillWidth
                )
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(0f, 0f),
                    size = Size(fillWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = CardBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
    }
}
