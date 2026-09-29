package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WhatsAppUtilTest {

    @Test
    fun `normalizarTelefone prefixes 55 to a local number`() {
        assertEquals("5511987654321", WhatsAppUtil.normalizarTelefone("(11) 98765-4321"))
    }

    @Test
    fun `normalizarTelefone keeps an already prefixed 55 number untouched`() {
        assertEquals("5511987654321", WhatsAppUtil.normalizarTelefone("55 11 98765-4321"))
    }

    @Test
    fun `normalizarTelefone strips non digit characters`() {
        assertEquals("5511999998888", WhatsAppUtil.normalizarTelefone("+55 (11) 99999-8888"))
    }
}
