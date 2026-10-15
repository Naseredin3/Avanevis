package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    isAnimating: Boolean = true,
    height: Dp = 48.dp,
    barWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val duration = 400 + (i * 70) % 600
            val delay = (i * 50) % 300

            val scale by if (isAnimating) {
                infiniteTransition.animateFloat(
                    initialValue = 0.2f + ((i % 5) * 0.1f),
                    targetValue = 0.95f - ((i % 3) * 0.08f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, delayMillis = delay, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$i"
                )
            } else {
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.3f) }
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
    }
}
