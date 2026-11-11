package org.fuelstat.core

import java.net.URI

/**
 * Cache key for an image delivered by the API.
 *
 * The API signs image addresses (`/api/v1/files/<uuid>.webp?exp=…&sig=…`) and the signature changes daily,
 * while the file behind it never changes. So the file name, not the full URL, identifies the image.
 */
public object ImageCacheKey {
    private val safeName = Regex("^[A-Za-z0-9_-][A-Za-z0-9._-]{0,79}$")
    private val unsafeChar = Regex("[^A-Za-z0-9._-]")

    public fun key(url: String): String {
        val path = runCatching { URI(url).rawPath }.getOrNull().orEmpty()
        val name = path.substringAfterLast('/')
        if (path.contains("/files/") && safeName.matches(name)) return name
        // Not a signed API address: fall back to the full address, made safe for use as a file name.
        return url.replace(unsafeChar, "_").takeLast(120)
    }
}
