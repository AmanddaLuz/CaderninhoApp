package com.caderninho.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneVisualTransformationTest {

    @Test
    fun `formats phone without changing the original digits`() {
        val transformed = PhoneVisualTransformation.filter(
            AnnotatedString("11999999999")
        )

        assertEquals("(11) 99999-9999", transformed.text.text)
        assertEquals(15, transformed.offsetMapping.originalToTransformed(11))
        assertEquals(11, transformed.offsetMapping.transformedToOriginal(15))
    }

    @Test
    fun `maps cursor positions across mask characters`() {
        val transformed = PhoneVisualTransformation.filter(
            AnnotatedString("119")
        )

        assertEquals(2, transformed.offsetMapping.originalToTransformed(1))
        assertEquals(3, transformed.offsetMapping.originalToTransformed(2))
        assertEquals(6, transformed.offsetMapping.originalToTransformed(3))
        assertEquals(2, transformed.offsetMapping.transformedToOriginal(5))
    }
}
