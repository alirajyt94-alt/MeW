package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.RoseError
import com.example.util.ByteFormatter

@Composable
fun CircularMemoryGauge(
    percentage: Float,
    usedBytes: Long,
    totalBytes: Long,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gauge_percent"
    )

    val gaugeColor = when {
        percentage >= 85f -> RoseError
        percentage >= 70f -> AmberWarning
        else -> EmeraldNeon
    }

    val gradientBrush = Brush.sweepGradient(
        listOf(
            CyanNeon,
            gaugeColor,
            gaugeColor
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("circular_memory_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 18.dp.toPx()
            val arcSize = size.toPx() - strokeWidth

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic Fill Arc
            val sweep = (260f * (animatedPercent / 100f)).coerceIn(0f, 260f)
            drawArc(
                brush = gradientBrush,
                startAngle = 140f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedPercent.toInt()}%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.testTag("gauge_percent_text")
            )
            Text(
                text = "RAM USAGE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            val (usedStr, usedUnit) = ByteFormatter.formatBytesPair(usedBytes)
            val (totalStr, totalUnit) = ByteFormatter.formatBytesPair(totalBytes)
            Text(
                text = "$usedStr $usedUnit / $totalStr $totalUnit",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun StorageProgressBar(
    percentage: Float,
    usedBytes: Long,
    totalBytes: Long,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "storage_percent"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Internal Storage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "${percentage.toInt()}% used",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (percentage > 85f) RoseError else CyanNeon,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedPercent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = CyanNeon,
            trackColor = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Used: ${ByteFormatter.formatBytes(usedBytes)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "Free: ${ByteFormatter.formatBytes((totalBytes - usedBytes).coerceAtLeast(0L))}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = EmeraldNeon,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun StatusBadge(
    isOptimized: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOptimized) Color(0xFF063A29) else Color(0xFF451A03)
    val contentColor = if (isOptimized) EmeraldNeon else AmberWarning
    val text = if (isOptimized) "System Running Smoothly" else "Optimization Recommended"
    val icon = if (isOptimized) Icons.Default.CheckCircle else Icons.Default.Warning

    Surface(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
