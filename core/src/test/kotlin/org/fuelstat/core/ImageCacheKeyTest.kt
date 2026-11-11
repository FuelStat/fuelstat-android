package org.fuelstat.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCacheKeyTest {
    @Test
    fun `signed API image is keyed by file name`() {
        val day1 = "https://tanken.1emc.de/api/v1/files/3f2a-11.webp?exp=1&sig=abc"
        val day2 = "https://tanken.1emc.de/api/v1/files/3f2a-11.webp?exp=2&sig=def"
        assertEquals("3f2a-11.webp", ImageCacheKey.key(day1))
        assertEquals(ImageCacheKey.key(day1), ImageCacheKey.key(day2))
    }

    @Test
    fun `other addresses become a safe file name`() {
        val key = ImageCacheKey.key("https://example.de/img/../a b.png")
        assertTrue(key.matches(Regex("[A-Za-z0-9._-]+")), key)
    }
}
