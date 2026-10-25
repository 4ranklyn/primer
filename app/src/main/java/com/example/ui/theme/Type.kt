package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Child-Facing Audio-Support Tier (Nunito Sans - Montessori tactile rounded letters)
val NunitoSansFontFamily = FontFamily(
    Font(R.font.nunito_sans, FontWeight.Normal),
    Font(R.font.nunito_sans, FontWeight.Bold),
    Font(R.font.nunito_sans, FontWeight.ExtraBold)
)

// Parent Dashboard & Diagnostic Tier (Plus Jakarta Sans - Humanist & tabular clarity)
val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

// Calm Scaffolding Canonical Typography Tokens
val DisplayChildXl = TextStyle(
    fontFamily = NunitoSansFontFamily,
    fontSize = 48.sp,
    fontWeight = FontWeight.ExtraBold,
    lineHeight = 56.sp,
    letterSpacing = (-0.02).sp
)

val DisplayChildXlMobile = TextStyle(
    fontFamily = NunitoSansFontFamily,
    fontSize = 32.sp,
    fontWeight = FontWeight.ExtraBold,
    lineHeight = 40.sp,
    letterSpacing = (-0.01).sp
)

val DisplayChildLg = TextStyle(
    fontFamily = NunitoSansFontFamily,
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 44.sp,
    letterSpacing = (-0.01).sp
)

val DisplayChildLgMobile = TextStyle(
    fontFamily = NunitoSansFontFamily,
    fontSize = 26.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 34.sp,
    letterSpacing = 0.sp
)

val HeadlineParentLg = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 30.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 38.sp,
    letterSpacing = (-0.02).sp
)

val HeadlineParentMd = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 22.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 30.sp,
    letterSpacing = (-0.01).sp
)

val HeadlineParentSm = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 26.sp,
    letterSpacing = 0.sp
)

val BodyParentLg = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

val BodyParentMd = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 22.sp,
    letterSpacing = 0.01.sp
)

val LabelParentSemibold = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 13.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 18.sp,
    letterSpacing = 0.02.sp
)

val LabelParentCaps = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 16.sp,
    letterSpacing = 0.06.sp
)

val MetricTabularLg = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 44.sp,
    letterSpacing = (-0.02).sp
)

val Typography = Typography(
    displayLarge = DisplayChildXlMobile,
    displayMedium = DisplayChildLgMobile,
    headlineLarge = HeadlineParentLg,
    headlineMedium = HeadlineParentMd,
    titleLarge = HeadlineParentSm,
    bodyLarge = BodyParentLg,
    bodyMedium = BodyParentMd,
    labelLarge = LabelParentSemibold,
    labelMedium = LabelParentCaps
)
