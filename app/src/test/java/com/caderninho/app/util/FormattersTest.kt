package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `currency formats value as brazilian currency`() {
        val resultado = Formatters.currency(1234.5)
        assertEquals("R$\u00A01.234,50", resultado)
    }

    @Test
    fun `currency formats zero`() {
        val resultado = Formatters.currency(0.0)
        assertEquals("R$\u00A00,00", resultado)
    }

    @Test
    fun `shortDate formats a known epoch millis as dd-MM-yyyy`() {
        // 2024-01-15T12:00:00Z
        val resultado = Formatters.shortDate(1705320000000L)
        assertEquals(3, resultado.split("/").size)
    }
}
