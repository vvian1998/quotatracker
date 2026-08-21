package com.quotatracker.app.ui.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.quotatracker.app.ui.theme.AmberAccent
import com.quotatracker.app.ui.theme.BackgroundDark
import com.quotatracker.app.ui.theme.CardBackground
import com.quotatracker.app.ui.theme.CardBorder
import com.quotatracker.app.ui.theme.CardShape
import com.quotatracker.app.ui.theme.ChipShape
import com.quotatracker.app.ui.theme.TealGlow
import com.quotatracker.app.ui.theme.TealPrimary
import com.quotatracker.app.ui.theme.TextPrimary
import com.quotatracker.app.ui.theme.TextSecondary
import com.quotatracker.app.util.PermissionUtils

@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageStats by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var hasNotification by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }

    // Re-check permissions on resume (when returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = PermissionUtils.hasUsageStatsPermission(context)
                hasOverlay = PermissionUtils.hasOverlayPermission(context)
                hasNotification = PermissionUtils.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotification = isGranted
    }

    val isAllGranted = hasUsageStats && hasOverlay && hasNotification

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TealGlow)
                    .border(1.5.dp, TealPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Setup Izin Aplikasi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usage Access adalah izin wajib untuk membaca statistik penggunaan. Overlay dan notifikasi bersifat opsional untuk fitur tambahan.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 1. Usage Stats Permission Card
            PermissionCard(
                required = true,
                icon = Icons.Default.DataUsage,
                title = "Akses Penggunaan Data",
                description = "Diperlukan untuk membaca kuota yang dihabiskan oleh masing-masing aplikasi.",
                isGranted = hasUsageStats,
                onGrantClick = { PermissionUtils.openUsageStatsSettings(context) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Overlay Permission Card
            PermissionCard(
                required = false,
                icon = Icons.Default.PictureInPicture,
                title = "Tampilkan di Atas Aplikasi",
                description = "Diperlukan untuk menampilkan balon melayang (floating bubble) saat membuka aplikasi lain.",
                isGranted = hasOverlay,
                onGrantClick = { PermissionUtils.openOverlaySettings(context) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Notification Permission Card
            PermissionCard(
                required = false,
                icon = Icons.Default.Notifications,
                title = "Notifikasi & Peringatan",
                description = "Diperlukan untuk menampilkan status pemantau dan peringatan saat kuota hampir habis.",
                isGranted = hasNotification,
                onGrantClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = onAllPermissionsGranted,
                enabled = hasUsageStats, // Primary requirement
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = BackgroundDark,
                    disabledContainerColor = CardBackground,
                    disabledContentColor = TextSecondary
                )
            ) {
                Text(
                    text = if (isAllGranted) "Lanjutkan ke Dashboard" else if (hasUsageStats) "Lanjutkan (Izin Lain Opsional)" else "Berikan Izin untuk Melanjutkan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    required: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) TealPrimary.copy(alpha = 0.5f) else CardBorder,
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, CardShape),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) TealGlow else AmberAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) TealPrimary else AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(ChipShape)
                        .background(if (isGranted) TealPrimary.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "Aktif" else "Belum",
                        color = if (isGranted) TealPrimary else AmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = (if (required) "Wajib. " else "Opsional. ") + description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            AnimatedVisibility(visible = !isGranted) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onGrantClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = ChipShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TealPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(TealPrimary)
                        )
                    ) {
                        Text(
                            text = "Izinkan Akses",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
