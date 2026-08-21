package com.quotatracker.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.Ember
import com.quotatracker.app.ui.theme.Hairline
import com.quotatracker.app.ui.theme.Ink
import com.quotatracker.app.ui.theme.Lime
import com.quotatracker.app.ui.theme.NeutralBucket
import com.quotatracker.app.ui.theme.SurfaceElevated
import com.quotatracker.app.ui.theme.TextMuted
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.ui.theme.VoucherShape
import com.quotatracker.app.util.DataFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Signature dashboard component — a "quota voucher" card.
 *
 * Replaces the old circular ring gauge with something grounded in the
 * Indonesian prepaid-data vernacular: an LCD-style counter readout and a
 * signal-bar strength indicator (instead of a colored status dot), plus a
 * segmented bar that actually encodes something useful — the Seluler
 * (mobile) vs WiFi split, since only mobile usage usually counts against
 * an operator's data quota.
 *
 * Entrance is one orchestrated moment (number counts up, bar fills) that
 * runs once and then stops — no looping glow or pulse afterward.
 */
@Composable
fun QuotaVoucherCard(
    usedBytes: Long,
    totalQuotaBytes: Long,
    mobileBytes: Long,
    wifiBytes: Long,
    subtitleLabel: String,
    modifier: Modifier = Modifier
) {
    val percentage = DataFormatter.calculatePercentage(usedBytes, totalQuotaBytes)
    val (usedVal, usedUnit) = DataFormatter.formatBytesParts(usedBytes)
    val usedValFloat = usedVal.toFloatOrNull() ?: 0f
    val formattedTotal = DataFormatter.formatBytes(totalQuotaBytes)

    val statusColor = if (percentage >= 0.9f) Ember else Lime
    val activeBars = when {
        percentage >= 0.9f -> 4
        percentage >= 0.6f -> 3
        percentage >= 0.3f -> 2
        else -> 1
    }
    val mobileFraction = if (usedBytes > 0) mobileBytes.toFloat() / usedBytes.toFloat() else 0f

    val animatedValue = remember { Animatable(0f) }
    val animatedMobileFraction = remember { Animatable(0f) }

    LaunchedEffect(usedValFloat) {
        animatedValue.animateTo(usedValFloat, animationSpec = tween(durationMillis = 900))
    }
    LaunchedEffect(mobileFraction) {
        animatedMobileFraction.animateTo(mobileFraction, animationSpec = tween(durationMillis = 700))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(VoucherShape)
            .background(SurfaceElevated)
            .border(1.dp, CardBorder, VoucherShape)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 18.dp)
        ) {
            // Eyebrow + signal strength
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitleLabel.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                SignalStrength(activeBars = activeBars, color = statusColor)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meter readout
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.1f", animatedValue.value),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = " $usedUnit",
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = " / $formattedTotal",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = "${(percentage * 100).roundToInt()}% terpakai",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Segmented bar — Seluler (counts against quota) vs WiFi
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val h = size.height
                val corner = CornerRadius(h / 2, h / 2)
                drawRoundRect(color = Ink, size = size, cornerRadius = corner)

                val mobileWidth = size.width * animatedMobileFraction.value
                if (mobileWidth > 0f) {
                    drawRoundRect(color = Lime, size = Size(mobileWidth, h), cornerRadius = corner)
                }
                val wifiWidth = size.width - mobileWidth
                if (wifiWidth > 0f) {
                    drawRoundRect(
                        color = NeutralBucket,
                        topLeft = Offset(mobileWidth, 0f),
                        size = Size(wifiWidth, h),
                        cornerRadius = corner
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LegendItem(color = Lime, label = "Seluler", value = DataFormatter.formatBytes(mobileBytes))
                LegendItem(color = NeutralBucket, label = "WiFi", value = DataFormatter.formatBytes(wifiBytes))
            }
        }
    }
}

@Composable
private fun SignalStrength(activeBars: Int, color: Color) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        val heights = listOf(5.dp, 8.dp, 11.dp, 14.dp)
        heights.forEachIndexed { index, h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h)
                    .background(
                        color = if (index < activeBars) color else Hairline,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}
