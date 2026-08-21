package com.quotatracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.ui.theme.Hairline
import com.quotatracker.app.ui.theme.Lime
import com.quotatracker.app.ui.theme.TextMuted
import com.quotatracker.app.ui.theme.TextPrimary

/**
 * Flat underline tabs — replaces the old pill-segmented selector.
 * One accent color (Lime) marks the active tab; nothing else moves.
 */
@Composable
fun PeriodSelector(
    selectedPeriod: UsagePeriod,
    onPeriodSelected: (UsagePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            UsagePeriod.values().forEach { period ->
                val isSelected = period == selectedPeriod

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) TextPrimary else TextMuted,
                    label = "tab_text"
                )
                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 0.dp,
                    label = "tab_indicator_width"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onPeriodSelected(period) }
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(indicatorWidth)
                            .background(Lime, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        Divider(color = Hairline, thickness = 1.dp)
    }
}
