package com.quotatracker.app.ui.screen.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.ui.components.AppUsageCard
import com.quotatracker.app.ui.components.PeriodSelector
import com.quotatracker.app.ui.components.QuotaGauge
import com.quotatracker.app.ui.theme.AmberAccent
import com.quotatracker.app.ui.theme.BackgroundDark
import com.quotatracker.app.ui.theme.CardBackground
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.CardShape
import com.quotatracker.app.ui.theme.GaugePillShape
import com.quotatracker.app.ui.theme.TealGlow
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TealVariant
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.DataFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAppClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleFloatingBubble(!uiState.isBubbleEnabled) },
                containerColor = if (uiState.isBubbleEnabled) TealPrimary else CardBackground,
                contentColor = if (uiState.isBubbleEnabled) BackgroundDark else TealPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .border(
                        width = 1.5.dp,
                        color = if (uiState.isBubbleEnabled) TealVariant else CardBorder,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Toggle Floating Bubble",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Bar Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QuotaTracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )

                    IconButton(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quota Ring Gauge
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val totalUsed = uiState.deviceSummary.grandTotal
                val totalQuota = uiState.quotaSetting.quotaLimitBytes

                QuotaGauge(
                    usedBytes = totalUsed,
                    totalQuotaBytes = totalQuota,
                    subtitle = when (uiState.selectedPeriod) {
                        UsagePeriod.DAILY -> "dipakai hari ini"
                        UsagePeriod.WEEKLY -> "dipakai minggu ini"
                        UsagePeriod.MONTHLY -> "dari kuota bulan ini"
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Download / Upload Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Download Pill
                    Surface(
                        shape = GaugePillShape,
                        color = CardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download",
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DataFormatter.formatBytes(uiState.deviceSummary.totalDownload),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Upload Pill
                    Surface(
                        shape = GaugePillShape,
                        color = CardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upload",
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DataFormatter.formatBytes(uiState.deviceSummary.totalUpload),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Period Selector (Hari Ini / Minggu / Bulan)
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.setPeriod(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Apps List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penggunaan Aplikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Text(
                        text = "${uiState.appUsageList.size} aplikasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // App Usage List Items
            if (uiState.isLoading && uiState.appUsageList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }
            } else if (uiState.appUsageList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data penggunaan tercatat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(
                    items = uiState.appUsageList,
                    key = { it.uid }
                ) { appUsage ->
                    AppUsageCard(
                        appUsage = appUsage,
                        maxUsageBytes = uiState.maxUsageBytes,
                        onClick = { onAppClick(appUsage.uid) }
                    )
                }
            }

            // Bottom Spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
