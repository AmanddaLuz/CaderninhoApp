package com.caderninho.app.util

object ValidadorCpf {

    fun normalizar(cpf: String): String = cpf.filter(Char::isDigit)

    fun validar(cpf: String): Boolean {
        val digitos = normalizar(cpf)
        if (digitos.length != TAMANHO_CPF || digitos.all { it == digitos.first() }) {
            return false
        }

        val primeiroDigito = calcularDigito(digitos, pesoInicial = 10)
        val segundoDigito = calcularDigito(digitos, pesoInicial = 11)
        return digitos[9].digitToInt() == primeiroDigito &&
            digitos[10].digitToInt() == segundoDigito
    }

    private fun calcularDigito(cpf: String, pesoInicial: Int): Int {
        val quantidade = pesoInicial - 1
        val soma = cpf.take(quantidade)
            .mapIndexed { indice, caractere -> caractere.digitToInt() * (pesoInicial - indice) }
            .sum()
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }

    private const val TAMANHO_CPF = 11
}
