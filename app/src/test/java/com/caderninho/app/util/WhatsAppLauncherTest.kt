package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WhatsAppLauncherTest {

    @Test
    fun `normalizePhone prefixes 55 to a local number`() {
        assertEquals("5511987654321", WhatsAppLauncher.normalizePhone("(11) 98765-4321"))
    }

    @Test
    fun `normalizePhone keeps an already prefixed 55 number untouched`() {
        assertEquals("5511987654321", WhatsAppLauncher.normalizePhone("55 11 98765-4321"))
    }

    @Test
    fun `normalizePhone strips non digit characters`() {
        assertEquals("5511999998888", WhatsAppLauncher.normalizePhone("+55 (11) 99999-8888"))
    }
}
