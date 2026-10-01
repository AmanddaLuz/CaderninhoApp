package com.caderninho.app.util

object CpfValidator {

    fun normalize(cpf: String): String = cpf.filter(Char::isDigit)

    fun validate(cpf: String): Boolean {
        val digits = normalize(cpf)
        if (digits.length != TAMANHO_CPF || digits.all { it == digits.first() }) {
            return false
        }

        val firstDigit = calculateDigit(digits, initialWeight = 10)
        val secondDigit = calculateDigit(digits, initialWeight = 11)
        return digits[9].digitToInt() == firstDigit &&
            digits[10].digitToInt() == secondDigit
    }

    private fun calculateDigit(cpf: String, initialWeight: Int): Int {
        val quantity = initialWeight - 1
        val sum = cpf.take(quantity)
            .mapIndexed { index, caractere -> caractere.digitToInt() * (initialWeight - index) }
            .sum()
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }

    private const val TAMANHO_CPF = 11
}
