package com.caderninho.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.caderninho.app.util.Formatters

object PhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = Formatters.phone(text.text)
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = PhoneOffsetMapping(formatted)
        )
    }
}

private class PhoneOffsetMapping(
    private val formatted: String
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0

        var digitsFound = 0
        var transformedOffset = formatted.length
        formatted.forEachIndexed { index, character ->
            if (character.isDigit()) digitsFound++
            if (digitsFound == offset && transformedOffset == formatted.length) {
                transformedOffset = index + 1
            }
        }
        return transformedOffset
    }

    override fun transformedToOriginal(offset: Int): Int =
        formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit)
}
