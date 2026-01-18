/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 26/12/2025
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

class MarkdownTransformation(
    private val textStyle: TextStyle,
    private val backgroundColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        val rawText = text.text

        val transformations = listOf(
            Transformation.Bold,
            Transformation.Italic,
            Transformation.Underline,
        )

        for (transformation in transformations) {
            val transformedTextStyle = when (transformation) {
                Transformation.Bold -> textStyle.copy(fontWeight = FontWeight.Bold)
                Transformation.Italic -> textStyle.copy(fontStyle = FontStyle.Italic)
                Transformation.Underline -> textStyle.copy(
                    textDecoration = TextDecoration.Underline,
                )
            }.toSpanStyle()
            for (match in Regex(transformation.pattern).findAll(rawText)) {
                builder.addStyle(
                    style = transformedTextStyle,
                    start = match.range.first,
                    end = match.range.last + 1,
                )
                builder.addStyle(
                    style = SpanStyle(color = backgroundColor),
                    start = match.range.first,
                    end = match.range.first + 1,
                )
                builder.addStyle(
                    style = SpanStyle(color = backgroundColor),
                    start = match.range.last,
                    end = match.range.last + 1,
                )
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

private sealed class Transformation {
    abstract val pattern: String

    data object Bold : Transformation() {
        override val pattern: String
            get() = getPatternFor("*")
    }
    data object Italic : Transformation() {
        override val pattern: String
            get() = getPatternFor("_")
    }
    data object Underline : Transformation() {
        override val pattern: String
            get() = getPatternFor("~")
    }

    fun getPatternFor(char: String): String = "\\$char([^$char]+)\\$char"
}