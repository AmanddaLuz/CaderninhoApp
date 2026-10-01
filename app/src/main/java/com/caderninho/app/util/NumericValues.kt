package com.caderninho.app.util

import java.math.BigDecimal
import java.math.RoundingMode

object NumericValues {

    fun formatCurrencyInput(value: String): String {
        val digits = value.filter(Char::isDigit)
            .takeLast(MAX_CURRENCY_DIGITS)
            .ifEmpty { "0" }
        val cents = digits.toLong()
        val wholeReais = cents / 100
        val fraction = cents % 100
        val formattedReais = wholeReais.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "R$ $formattedReais,${fraction.toString().padStart(2, '0')}"
    }

    fun quantity(value: String): Double? =
        value.normalizeDecimal().toBigDecimalOrNull()
            ?.takeIf { it > BigDecimal.ZERO }
            ?.toDouble()

    fun cents(value: String): Long? =
        value.normalizeDecimal().toBigDecimalOrNull()
            ?.takeIf { it > BigDecimal.ZERO }
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.movePointRight(2)
            ?.longValueExact()

    private fun String.normalizeDecimal(): String {
        val cleaned = trim().replace("R$", "").replace(" ", "")
        return if (cleaned.contains(',')) {
            cleaned.replace(".", "").replace(',', '.')
        } else {
            cleaned
        }
    }

    private const val MAX_CURRENCY_DIGITS = 15
}
