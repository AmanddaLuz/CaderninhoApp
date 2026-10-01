package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumericValuesTest {

    @Test
    fun `formatCurrencyInput applies Brazilian currency separators`() {
        assertEquals("R$ 0,01", NumericValues.formatCurrencyInput("1"))
        assertEquals("R$ 12,34", NumericValues.formatCurrencyInput("R$ 1,234"))
        assertEquals("R$ 1.234,56", NumericValues.formatCurrencyInput("123456"))
    }

    @Test
    fun `cents parses Brazilian and dot decimal values`() {
        assertEquals(123_456L, NumericValues.cents("R$ 1.234,56"))
        assertEquals(1_050L, NumericValues.cents("10.50"))
    }

    @Test
    fun `quantity accepts comma decimal and rejects non positive values`() {
        assertEquals(0.5, NumericValues.quantity("0,5")!!, 0.0)
        assertNull(NumericValues.quantity("0"))
        assertNull(NumericValues.quantity("-1"))
    }
}
