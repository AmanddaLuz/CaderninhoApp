package com.caderninho.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.caderninho.app.ui.theme.MarginRed
import com.caderninho.app.ui.theme.RuleBlueDark
import com.caderninho.app.ui.theme.RuleBlueLight

@Composable
fun NotebookPage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val isDark = background.luminance() < DARK_LUMINANCE_THRESHOLD
    val ruleColor = if (isDark) RuleBlueDark else RuleBlueLight
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .drawBehind {
                val spacing = LINE_SPACING.toPx()
                var y = spacing
                while (y < size.height) {
                    drawLine(ruleColor, start = Offset(0f, y), end = Offset(size.width, y))
                    y += spacing
                }
                drawLine(
                    MarginRed.copy(alpha = MARGIN_ALPHA),
                    start = Offset(MARGIN_START.toPx(), 0f),
                    end = Offset(MARGIN_START.toPx(), size.height),
                    strokeWidth = MARGIN_WIDTH.toPx()
                )
            }
    ) {
        content()
    }
}

@Composable
fun NotebookFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier
                )
            }
        } else {
            null
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    )
}

private fun Color.luminance(): Float =
    (red * RED_WEIGHT) + (green * GREEN_WEIGHT) + (blue * BLUE_WEIGHT)

private val LINE_SPACING = 28.dp
private val MARGIN_START = 12.dp
private val MARGIN_WIDTH = 1.dp
private const val DARK_LUMINANCE_THRESHOLD = 0.35f
private const val MARGIN_ALPHA = 0.55f
private const val RED_WEIGHT = 0.2126f
private const val GREEN_WEIGHT = 0.7152f
private const val BLUE_WEIGHT = 0.0722f
