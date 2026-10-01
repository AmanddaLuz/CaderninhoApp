package com.caderninho.app.util

import java.math.BigDecimal
import java.math.RoundingMode

object ValoresNumericos {

    fun quantidade(valor: String): Double? =
        valor.normalizarDecimal().toBigDecimalOrNull()
            ?.takeIf { it > BigDecimal.ZERO }
            ?.toDouble()

    fun centavos(valor: String): Long? =
        valor.normalizarDecimal().toBigDecimalOrNull()
            ?.takeIf { it > BigDecimal.ZERO }
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.movePointRight(2)
            ?.longValueExact()

    private fun String.normalizarDecimal(): String {
        val limpo = trim().replace("R$", "").replace(" ", "")
        return if (limpo.contains(',')) {
            limpo.replace(".", "").replace(',', '.')
        } else {
            limpo
        }
    }
}
