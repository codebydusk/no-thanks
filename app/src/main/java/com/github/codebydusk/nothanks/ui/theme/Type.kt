package com.github.codebydusk.nothanks.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.io.File

/**
 * Loads the Metamorphous font from assets/fonts/ at runtime.
 * Falls back to Serif if the file is missing.
 */
object AppFonts {
    private var _metamorphous: FontFamily? = null

    fun metamorphous(context: Context): FontFamily {
        if (_metamorphous != null) return _metamorphous!!
        return try {
            val tempFile = File(context.cacheDir, "Metamorphous-Regular.ttf")
            if (!tempFile.exists()) {
                context.assets.open("fonts/Metamorphous-Regular.ttf").use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val family = FontFamily(Font(tempFile))
            _metamorphous = family
            family
        } catch (e: Exception) {
            FontFamily.Serif  // graceful fallback
        }
    }
}

// Default typography — used when fonts haven't been resolved from assets yet.
// The settings screen overrides these with theme-specific fonts at composition time.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)