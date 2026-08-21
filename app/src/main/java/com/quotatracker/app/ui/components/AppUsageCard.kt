package com.quotatracker.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.ui.theme.AvatarShape
import com.quotatracker.app.ui.theme.Ember
import com.quotatracker.app.ui.theme.FlatCardShape
import com.quotatracker.app.ui.theme.Hairline
import com.quotatracker.app.ui.theme.Ink
import com.quotatracker.app.ui.theme.Lime
import com.quotatracker.app.ui.theme.Surface
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Flat app usage row — no elevation, no glow, hairline divider only.
 * Entrance is staggered by [index] so the list reads as data settling
 * in, once, rather than a decoration that keeps moving.
 */
@Composable
fun AppUsageCard(
    appUsage: AppDataUsage,
    maxUsageBytes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    val progressRatio = if (maxUsageBytes > 0) {
        (appUsage.totalBytes.toFloat() / maxUsageBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var visible by remember(appUsage.uid) { mutableStateOf(false) }
    LaunchedEffect(appUsage.uid) {
        delay(120L + index * 70L)
        visible = true
    }

    val enterAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "card_alpha"
    )
    val enterOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "card_offset"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = if (visible) progressRatio else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    val barColor = if (progressRatio > 0.85f) Ember else Lime

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(enterAlpha)
            .offset(y = enterOffset)
            .clip(FlatCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AppAvatar(appUsage = appUsage)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = appUsage.appName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "Seluler ${DataFormatter.formatBytes(appUsage.mobileBytes)} · WiFi ${DataFormatter.formatBytes(appUsage.wifiBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = DataFormatter.formatBytes(appUsage.totalBytes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Thin progress track — single accent color, ember only past
        // an 85% share-of-list threshold. No gradient.
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height
            val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

            drawRoundRect(
                color = Ink,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )

            if (animatedProgress > 0f) {
                val fillWidth = barWidth * animatedProgress
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(fillWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Hairline.copy(alpha = 0.6f), thickness = 0.5.dp)
    }
}

@Composable
private fun AppAvatar(appUsage: AppDataUsage) {
    val iconDrawable = appUsage.appIcon

    if (iconDrawable != null) {
        val bitmapState by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = iconDrawable) {
            value = withContext(Dispatchers.IO) {
                try {
                    iconDrawable.toBitmap(width = 80, height = 80)
                } catch (e: Exception) {
                    null
                }
            }
        }
        val currentBitmap = bitmapState
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = appUsage.appName,
                modifier = Modifier
                    .size(38.dp)
                    .clip(AvatarShape)
            )
            return
        }
    }

    // Fallback: flat monogram avatar instead of a generic system icon
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(AvatarShape)
            .background(Surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = appUsage.appName.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
    }
}
