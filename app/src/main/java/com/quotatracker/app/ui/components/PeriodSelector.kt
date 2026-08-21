package com.quotatracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.ui.theme.BackgroundDark
import com.quotatracker.app.ui.theme.CardBackground
import com.quotatracker.app.ui.theme.ChipShape
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary

@Composable
fun PeriodSelector(
    selectedPeriod: UsagePeriod,
    onPeriodSelected: (UsagePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ChipShape)
            .background(CardBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UsagePeriod.values().forEach { period ->
            val isSelected = period == selectedPeriod

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) TealPrimary else Color.Transparent,
                label = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) BackgroundDark else TextSecondary,
                label = "chip_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(ChipShape)
                    .background(bgColor)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}
