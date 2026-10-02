package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelBorderHighlight
import com.example.ui.theme.PixelSurface

@Composable
fun ParchmentBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PixelSurface,
    borderColor: Color = PixelBorder,
    highlightBorder: Boolean = false,
    contentPadding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val activeBorder = if (highlightBorder) PixelBorderHighlight else borderColor
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(BorderStroke(1.5.dp, activeBorder), RoundedCornerShape(6.dp))
            .padding(contentPadding),
        content = content
    )
}
