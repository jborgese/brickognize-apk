package com.frootsnoops.brickognize.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.bitmapConfig
import coil3.request.error
import coil3.request.placeholder
import com.frootsnoops.brickognize.R
import com.frootsnoops.brickognize.domain.model.BrickItem

/**
 * Displays a [BrickItem] image with automatic fallback to BrickLink CDN
 * when the primary URL fails (e.g. stale Brickognize CDN versions).
 */
@Composable
fun BrickPartImage(
    part: BrickItem,
    modifier: Modifier = Modifier,
    sizePx: Int? = null,
    contentDescription: String? = part.name,
    contentScale: ContentScale = ContentScale.Crop,
    bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565
) {
    var useFallback by remember(part.id, part.imgUrl) { mutableStateOf(false) }
    val url = if (useFallback) part.brickLinkImgUrl else part.displayImgUrl
    val context = LocalContext.current

    val requestBuilder = ImageRequest.Builder(context)
        .data(url)
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_error)
        .bitmapConfig(bitmapConfig)
        .listener(onError = { _, _ ->
            if (!useFallback && part.imgUrl != null) {
                useFallback = true
            }
        })

    if (sizePx != null) {
        requestBuilder.size(sizePx, sizePx)
    }

    AsyncImage(
        model = requestBuilder.build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
