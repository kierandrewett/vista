package dev.drewett.vista.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap

/** Renders an already-loaded [Drawable] (app banner/icon) without a network image loader. */
@Composable
fun DrawableImage(
    drawable: Drawable,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image = remember(drawable) { drawable.toSafeBitmap().asImageBitmap() }
    Image(
        bitmap = image,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

private fun Drawable.toSafeBitmap(fallback: Int = 128): Bitmap {
    val w = if (intrinsicWidth > 0) intrinsicWidth else fallback
    val h = if (intrinsicHeight > 0) intrinsicHeight else fallback
    return toBitmap(w, h, Bitmap.Config.ARGB_8888)
}
