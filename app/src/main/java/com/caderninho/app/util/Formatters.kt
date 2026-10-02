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

    fun phone(value: String): String {
        val digits = value.filter(Char::isDigit).take(PHONE_MAX_DIGITS)
        return when {
            digits.isEmpty() -> ""
            digits.length <= AREA_CODE_DIGITS -> "($digits"
            digits.length <= PHONE_PREFIX_END -> {
                "(${digits.take(AREA_CODE_DIGITS)}) ${digits.drop(AREA_CODE_DIGITS)}"
            }
            else -> {
                val areaCode = digits.take(AREA_CODE_DIGITS)
                val number = digits.drop(AREA_CODE_DIGITS)
                "($areaCode) ${number.take(MOBILE_PREFIX_DIGITS)}-${number.drop(MOBILE_PREFIX_DIGITS)}"
            }
        }
    }

    private const val PHONE_MAX_DIGITS = 11
    private const val AREA_CODE_DIGITS = 2
    private const val MOBILE_PREFIX_DIGITS = 5
    private const val PHONE_PREFIX_END = AREA_CODE_DIGITS + MOBILE_PREFIX_DIGITS
}
