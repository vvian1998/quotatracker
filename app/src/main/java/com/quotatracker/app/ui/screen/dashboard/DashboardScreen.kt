package com.quotatracker.app.ui.screen.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.ui.components.AppUsageCard
import com.quotatracker.app.ui.components.PeriodSelector
import com.quotatracker.app.ui.components.QuotaVoucherCard
import com.quotatracker.app.ui.theme.FabShape
import com.quotatracker.app.ui.theme.Ink
import com.quotatracker.app.ui.theme.Lime
import com.quotatracker.app.ui.theme.Surface
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAppClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Ink,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleFloatingBubble(!uiState.isBubbleEnabled) },
                containerColor = if (uiState.isBubbleEnabled) Lime else Surface,
                contentColor = if (uiState.isBubbleEnabled) Ink else Lime,
                shape = FabShape,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = if (uiState.isBubbleEnabled) "Floating bubble aktif" else "Floating bubble nonaktif",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppWordmark()

                    IconButton(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(FabShape)
                            .background(Surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Signature quota voucher card
            item {
                val totalUsed = uiState.deviceSummary.grandTotal
                val totalQuota = uiState.quotaSetting.quotaLimitBytes

                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                    QuotaVoucherCard(
                        usedBytes = totalUsed,
                        totalQuotaBytes = totalQuota,
                        mobileBytes = uiState.deviceSummary.totalMobile,
                        wifiBytes = uiState.deviceSummary.totalWifi,
                        subtitleLabel = when (uiState.selectedPeriod) {
                            UsagePeriod.DAILY -> "Pemakaian Hari Ini • Batas Referensi Bulanan"
                            UsagePeriod.WEEKLY -> "Pemakaian Minggu Ini • Batas Referensi Bulanan"
                            UsagePeriod.MONTHLY -> "Pemakaian Siklus Bulanan"
                        }
                    )
                }

                Text(
                    text = "Catatan: total perangkat dapat berbeda dari jumlah aplikasi karena NetworkStats juga mencakup komponen sistem dan UID yang tidak terlihat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))
            }

            // Period tabs
            item {
                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
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
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Penggunaan Aplikasi",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.isLoading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                color = Lime,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "${uiState.appUsageList.size} aplikasi",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // App Usage List Items
            uiState.errorMessage?.let { message ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Lime)
                        ) {
                            Text(text = "Coba lagi", color = Ink)
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.appUsageList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Lime)
                    }
                }
            } else if (uiState.appUsageList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Belum ada data penggunaan tercatat",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mungkin perlu beberapa menit untuk sinkronisasi pertama atau pastikan izin Akses Penggunaan (Usage Access) telah diberikan.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.appUsageList) { index, appUsage ->
                    AppUsageCard(
                        appUsage = appUsage,
                        maxUsageBytes = uiState.maxUsageBytes,
                        onClick = { onAppClick(appUsage.uid) },
                        index = index,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // Bottom Spacing for FAB and BottomNavBar
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun AppWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            val heights = listOf(5.dp, 8.dp, 11.dp, 14.dp)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(1.dp))
                        .background(Lime)
                )
            }
        }
        Text(
            text = "QuotaTracker",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        )
    }
}
