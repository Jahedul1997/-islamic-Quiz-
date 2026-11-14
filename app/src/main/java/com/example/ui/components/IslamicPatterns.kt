package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicGeometricBackground(
    modifier: Modifier = Modifier,
    lineColor: Color = Gold400.copy(alpha = 0.10f)
) {
    Canvas(modifier = modifier) {
        val step = 72.dp.toPx()
        val cols = (size.width / step).toInt() + 2
        val rows = (size.height / step).toInt() + 2

        for (r in 0..rows) {
            for (c in 0..cols) {
                val cx = c * step
                val cy = r * step
                val radius = step * 0.34f
                drawOctagram(Offset(cx, cy), radius, lineColor)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOctagram(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path1 = Path()
    val path2 = Path()
    for (i in 0 until 4) {
        val a1 = Math.toRadians(i * 90.0)
        val x1 = center.x + (radius * cos(a1)).toFloat()
        val y1 = center.y + (radius * sin(a1)).toFloat()
        if (i == 0) path1.moveTo(x1, y1) else path1.lineTo(x1, y1)

        val a2 = Math.toRadians(i * 90.0 + 45.0)
        val x2 = center.x + (radius * cos(a2)).toFloat()
        val y2 = center.y + (radius * sin(a2)).toFloat()
        if (i == 0) path2.moveTo(x2, y2) else path2.lineTo(x2, y2)
    }
    path1.close()
    path2.close()
    drawPath(path1, color = color, style = Stroke(width = 1.4f))
    drawPath(path2, color = color, style = Stroke(width = 1.4f))
}

@Composable
fun IslamicStarBadge(
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Emerald800,
    borderColor: Color = Gold400,
    textColor: Color = Gold300
) {
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 2.dp.toPx()
            val c = Offset(size.width / 2f, size.height / 2f)
            val path = Path()
            for (i in 0 until 16) {
                val angle = Math.toRadians(i * 22.5)
                val rad = if (i % 2 == 0) r else r * 0.78f
                val x = c.x + (rad * cos(angle)).toFloat()
                val y = c.y + (rad * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = bgColor)
            drawPath(path, color = borderColor, style = Stroke(width = 1.6.dp.toPx()))
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IslamicScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Emerald900, Emerald800, Emerald700))
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        IslamicGeometricBackground(
            modifier = Modifier.matchParentSize(),
            lineColor = Gold400.copy(alpha = 0.08f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("header_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Gold300
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

@Composable
fun EmeraldGoldGradientCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Emerald900, Emerald800, Color(0xFF104F3A))
                )
            )
    ) {
        IslamicGeometricBackground(
            modifier = Modifier.matchParentSize(),
            lineColor = Gold400.copy(alpha = 0.11f)
        )
        content()
    }
}
