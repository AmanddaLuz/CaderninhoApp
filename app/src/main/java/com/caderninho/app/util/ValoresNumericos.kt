package com.caderninho.app.util

import java.math.BigDecimal
import java.math.RoundingMode

object ValoresNumericos {

    fun formatarMoedaDigitada(valor: String): String {
        val digitos = valor.filter(Char::isDigit)
            .takeLast(MAXIMO_DIGITOS_MOEDA)
            .ifEmpty { "0" }
        val centavos = digitos.toLong()
        val reais = centavos / 100
        val fracao = centavos % 100
        val reaisFormatados = reais.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "R$ $reaisFormatados,${fracao.toString().padStart(2, '0')}"
    }

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

    private const val MAXIMO_DIGITOS_MOEDA = 15
}
