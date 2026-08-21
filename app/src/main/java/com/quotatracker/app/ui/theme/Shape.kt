package com.quotatracker.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// v2 tokens
val VoucherShape = RoundedCornerShape(20.dp)
val FlatCardShape = RoundedCornerShape(10.dp)
val AvatarShape = RoundedCornerShape(10.dp)
val FabShape = RoundedCornerShape(16.dp)

// legacy aliases — kept for existing screens
val CardShape = RoundedCornerShape(16.dp)
val ChipShape = RoundedCornerShape(24.dp)
val BubbleShape = RoundedCornerShape(14.dp)
val GaugePillShape = RoundedCornerShape(20.dp)
