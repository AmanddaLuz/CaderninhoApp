package com.caderninho.app.util

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Utilitários de formatação de moeda e datas em português (BR). */
object Formatadores {

    private val moeda = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    private val dataCurta = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())

    fun moeda(valor: Double): String = moeda.format(valor)

    fun moedaCentavos(valorCentavos: Long): String = moeda(valorCentavos / 100.0)

    fun dataCurta(timestampMillis: Long): String =
        dataCurta.format(Instant.ofEpochMilli(timestampMillis))
}
