package com.caderninho.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CpfValidatorTest {

    @Test
    fun `validate accepts formatted and unformatted valid CPF`() {
        assertTrue(CpfValidator.validate("529.982.247-25"))
        assertTrue(CpfValidator.validate("52998224725"))
    }

    @Test
    fun `validate rejects invalid length check digits and repeated digits`() {
        assertFalse(CpfValidator.validate("123"))
        assertFalse(CpfValidator.validate("52998224724"))
        assertFalse(CpfValidator.validate("11111111111"))
    }

    @Test
    fun `normalize keeps only digits`() {
        assertEquals("52998224725", CpfValidator.normalize("529.982.247-25"))
    }
}
