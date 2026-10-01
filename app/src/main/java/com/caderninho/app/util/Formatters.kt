package com.caderninho.app.util

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Utilitários de formatação de moeda e datas em português (BR). */
object Formatters {

    private val currency = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    private val shortDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())

    fun currency(value: Double): String = currency.format(value)

    fun currencyFromCents(valueCents: Long): String = currency(valueCents / 100.0)

    fun shortDate(timestampMillis: Long): String =
        shortDate.format(Instant.ofEpochMilli(timestampMillis))
}
