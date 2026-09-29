package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatadoresTest {

    @Test
    fun `moeda formats value as brazilian currency`() {
        val resultado = Formatadores.moeda(1234.5)
        assertEquals("R$\u00A01.234,50", resultado)
    }

    @Test
    fun `moeda formats zero`() {
        val resultado = Formatadores.moeda(0.0)
        assertEquals("R$\u00A00,00", resultado)
    }

    @Test
    fun `dataCurta formats a known epoch millis as dd-MM-yyyy`() {
        // 2024-01-15T12:00:00Z
        val resultado = Formatadores.dataCurta(1705320000000L)
        assertEquals(3, resultado.split("/").size)
    }
}
