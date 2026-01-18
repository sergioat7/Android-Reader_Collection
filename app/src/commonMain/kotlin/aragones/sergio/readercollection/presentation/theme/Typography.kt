/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/8/2024
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.roboto_serif_bold
import reader_collection.app.generated.resources.roboto_serif_regular
import reader_collection.app.generated.resources.roboto_serif_thin

@Composable
private fun CustomFont() = FontFamily(
    Font(Res.font.roboto_serif_regular, FontWeight.Normal),
    Font(Res.font.roboto_serif_thin, FontWeight.Thin),
    Font(Res.font.roboto_serif_bold, FontWeight.Bold),
)

@Composable
fun Typography() = Typography(
    displayLarge = TextStyle.H1.copy(fontFamily = CustomFont()),
    displayMedium = TextStyle.H2.copy(fontFamily = CustomFont()),
    displaySmall = TextStyle.H3.copy(fontFamily = CustomFont()),
    bodyLarge = TextStyle.Body.copy(fontFamily = CustomFont()),
    bodyMedium = TextStyle.BodySmall.copy(fontFamily = CustomFont()),
    labelLarge = TextStyle.Button.copy(fontFamily = CustomFont()),
)

private val TextStyle.Companion.H1: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    )

private val TextStyle.Companion.H2: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    )

private val TextStyle.Companion.H3: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    )

private val TextStyle.Companion.Body: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    )

private val TextStyle.Companion.BodySmall: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    )

private val TextStyle.Companion.Button: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
    )