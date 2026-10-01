package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValoresNumericosTest {

    @Test
    fun `centavos parses Brazilian and dot decimal values`() {
        assertEquals(123_456L, ValoresNumericos.centavos("R$ 1.234,56"))
        assertEquals(1_050L, ValoresNumericos.centavos("10.50"))
    }

    @Test
    fun `quantidade accepts comma decimal and rejects non positive values`() {
        assertEquals(0.5, ValoresNumericos.quantidade("0,5")!!, 0.0)
        assertNull(ValoresNumericos.quantidade("0"))
        assertNull(ValoresNumericos.quantidade("-1"))
    }
}
