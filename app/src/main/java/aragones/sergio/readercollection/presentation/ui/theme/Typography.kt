/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/8/2024
 */

package aragones.sergio.readercollection.presentation.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R

private val fonts = FontFamily(
    Font(R.font.roboto_serif_regular, FontWeight.Normal),
    Font(R.font.roboto_serif_thin, FontWeight.Thin),
    Font(R.font.roboto_serif_bold, FontWeight.Bold),
)

val Typography = Typography(
    defaultFontFamily = fonts,
    h1 = TextStyle.H1,
    h2 = TextStyle.H2,
    h3 = TextStyle.H3,
    body1 = TextStyle.Body,
    body2 = TextStyle.BodySmall,
    button = TextStyle.Button,
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