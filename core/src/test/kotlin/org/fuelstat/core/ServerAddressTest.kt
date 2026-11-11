package org.fuelstat.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerAddressTest {
    @Test
    fun `host alone becomes https API URL`() {
        assertEquals("https://tanken.example.de/api/v1", ServerAddress.apiBaseUrl("tanken.example.de"))
    }

    @Test
    fun `trailing slash, upper case host and existing API path are normalised`() {
        assertEquals("https://tanken.example.de/api/v1", ServerAddress.apiBaseUrl(" https://Tanken.Example.de/ "))
        assertEquals("https://tanken.example.de/api/v1", ServerAddress.apiBaseUrl("https://tanken.example.de/api/v1/"))
    }

    @Test
    fun `port and sub path are kept`() {
        assertEquals(
            "https://example.de:8443/fuelstat/api/v1",
            ServerAddress.apiBaseUrl("example.de:8443/fuelstat"),
        )
    }

    @Test
    fun `unusable input is rejected`() {
        assertNull(ServerAddress.apiBaseUrl(""))
        assertNull(ServerAddress.apiBaseUrl("http://tanken.example.de"))
        assertNull(ServerAddress.apiBaseUrl("tanken example.de"))
        assertNull(ServerAddress.apiBaseUrl("https://user:pw@tanken.example.de"))
        assertNull(ServerAddress.apiBaseUrl("https://tanken.example.de/?x=1"))
        assertNull(ServerAddress.apiBaseUrl("https://"))
    }

    @Test
    fun `display name drops scheme and API path`() {
        assertEquals("tanken.1emc.de", ServerAddress.displayName(ServerAddress.DEFAULT_API_BASE_URL))
        assertEquals("example.de:8443/fuelstat", ServerAddress.displayName("https://example.de:8443/fuelstat/api/v1"))
    }

    @Test
    fun `web page URL is on the same server`() {
        assertEquals(
            "https://tanken.1emc.de/pages/impressum.php",
            ServerAddress.webPageUrl("/pages/impressum.php", ServerAddress.DEFAULT_API_BASE_URL),
        )
    }
}
