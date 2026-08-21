package com.quotatracker.app.ui.screen.detail

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CellWifi
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.quotatracker.app.ui.components.WeeklyUsageChart
import com.quotatracker.app.ui.theme.BackgroundDark
import com.quotatracker.app.ui.theme.BlueWifi
import com.quotatracker.app.ui.theme.CardBackground
import com.quotatracker.app.ui.theme.CardBackgroundElevated
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.CardShape
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter
import java.util.Locale

@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (uiState.isLoading && uiState.appUsage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else {
            val appUsage = uiState.appUsage

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Bar: Back Button + App Icon + Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (appUsage?.appIcon != null) {
                        val bitmap = appUsage.appIcon.toBitmap(width = 72, height = 72)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = appUsage.appName,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = appUsage?.appName ?: "Detail Aplikasi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mobile vs WiFi Split Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricSplitCard(
                        title = "Mobile",
                        amount = DataFormatter.formatBytes(appUsage?.mobileBytes ?: 0L),
                        icon = Icons.Default.SignalCellularAlt,
                        accentColor = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricSplitCard(
                        title = "WiFi",
                        amount = DataFormatter.formatBytes(appUsage?.wifiBytes ?: 0L),
                        icon = Icons.Default.CellWifi,
                        accentColor = BlueWifi,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly 7-Day Chart
                WeeklyUsageChart(
                    weeklyBreakdown = uiState.weeklyBreakdown
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Foreground / Background Breakdown Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, CardShape),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Rincian Aktivitas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        BreakdownRow(
                            icon = Icons.Default.StayCurrentPortrait,
                            title = "Foreground (Layar Aktif)",
                            amount = DataFormatter.formatBytes(appUsage?.foregroundBytes ?: 0L),
                            totalBytes = appUsage?.totalBytes ?: 1L,
                            usedBytes = appUsage?.foregroundBytes ?: 0L,
                            color = TealPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        BreakdownRow(
                            icon = Icons.Default.Bedtime,
                            title = "Background (Latar Belakang)",
                            amount = DataFormatter.formatBytes(appUsage?.backgroundBytes ?: 0L),
                            totalBytes = appUsage?.totalBytes ?: 1L,
                            usedBytes = appUsage?.backgroundBytes ?: 0L,
                            color = BlueWifi
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Set Quota Limit Card
                Card(
                    modifier = Modifier
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Set Kuota Limit Aplikasi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Batas: ${String.format(Locale.US, "%.1f GB", uiState.quotaLimitGb)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Switch(
                                checked = uiState.isQuotaLimitEnabled,
                                onCheckedChange = { viewModel.setQuotaLimitEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundDark,
                                    checkedTrackColor = TealPrimary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CardBackgroundElevated
                                )
                            )
                        }

                        if (uiState.isQuotaLimitEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Slider(
                                value = uiState.quotaLimitGb,
                                onValueChange = { viewModel.setQuotaLimitGb(it) },
                                valueRange = 0.1f..10.0f,
                                steps = 99,
                                colors = SliderDefaults.colors(
                                    thumbColor = TealPrimary,
                                    activeTrackColor = TealPrimary,
                                    inactiveTrackColor = CardBackgroundElevated
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MetricSplitCard(
    title: String,
    amount: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, CardBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    icon: ImageVector,
    title: String,
    amount: String,
    totalBytes: Long,
    usedBytes: Long,
    color: androidx.compose.ui.graphics.Color
) {
    val ratio = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Small bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(CardBackgroundElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceAtLeast(0.02f))
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
