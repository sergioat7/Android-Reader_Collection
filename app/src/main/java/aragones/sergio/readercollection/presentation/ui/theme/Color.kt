/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/8/2024
 */

package aragones.sergio.readercollection.presentation.ui.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val EbonyClay = Color(0xFF2C2C42)//should be 0xFF2C2C42
internal val LightEbonyClay = Color(0x752C2C42)//should be 0x752C2C42
internal val White = Color(0xFFFFFFFF)
internal val LightWhite = Color(0x75FFFFFF)
internal val RoseBud = Color(0xFFF9B197)//should be 0xFFFBB2A3
internal val LightRoseBud = Color(0x75F9B197)//should be 0x75FBB2A3
internal val Scorpion = Color(0xFF585858)//should be 0xFF695F62
internal val PaleSlate = Color(0xFFCECDCE)//should be 0xFFC3BFC1
internal val Boulder = Color(0xFF757575)//should be 0xFF7A7A7A

val Colors.roseBud: Color
    @Composable get() = if (isLight) RoseBud else RoseBud
val Colors.lightRoseBud: Color
    @Composable get() = if (isLight) LightRoseBud else LightRoseBud
val Colors.description: Color
    @Composable get() = if(isLight) Scorpion else PaleSlate
val Colors.selector: Color
    @Composable get() = if(isLight) PaleSlate else Boulder