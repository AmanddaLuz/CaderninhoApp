package com.caderninho.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Selo genérico e reutilizável para indicar um status (pago, pendente, etc). */
@Composable
fun SeloStatus(
    texto: String,
    corFundo: Color,
    corTexto: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        color = corFundo,
        contentColor = corTexto,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
