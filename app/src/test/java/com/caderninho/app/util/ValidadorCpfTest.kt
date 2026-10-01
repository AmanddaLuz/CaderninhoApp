package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCpfTest {

    @Test
    fun `validar accepts formatted and unformatted valid CPF`() {
        assertTrue(ValidadorCpf.validar("529.982.247-25"))
        assertTrue(ValidadorCpf.validar("52998224725"))
    }

    @Test
    fun `validar rejects invalid length check digits and repeated digits`() {
        assertFalse(ValidadorCpf.validar("123"))
        assertFalse(ValidadorCpf.validar("52998224724"))
        assertFalse(ValidadorCpf.validar("11111111111"))
    }

    @Test
    fun `normalizar keeps only digits`() {
        assertEquals("52998224725", ValidadorCpf.normalizar("529.982.247-25"))
    }
}
