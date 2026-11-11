package org.fuelstat.core

import java.net.URI
import java.net.URISyntaxException
import java.util.Locale

/** Address of a Fuelstat server (the hosted service or a self-hosted installation). */
public object ServerAddress {
    /** Path of the REST API below the server root. */
    public const val API_PATH: String = "/api/v1"

    /** API base URL of the hosted service. */
    public const val DEFAULT_API_BASE_URL: String = "https://tanken.1emc.de/api/v1"

    /**
     * Turns user input such as `tanken.example.de`, `https://tanken.example.de/` or
     * `https://tanken.example.de/api/v1` into the API base URL (`https://tanken.example.de/api/v1`).
     *
     * Only HTTPS is accepted: tokens and passwords must not travel unencrypted.
     * Returns null for anything that is not a usable address.
     */
    public fun apiBaseUrl(input: String): String? {
        var text = input.trim()
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        if (!text.contains("://")) text = "https://$text"

        val uri = try {
            URI(text)
        } catch (_: URISyntaxException) {
            return null
        }
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        val host = uri.host?.takeIf { it.isNotEmpty() } ?: return null
        if (uri.rawQuery != null || uri.rawFragment != null || uri.rawUserInfo != null) return null

        var path = uri.rawPath.orEmpty().trimEnd('/')
        if (!path.endsWith(API_PATH)) path += API_PATH
        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "https://${host.lowercase(Locale.ROOT)}$port$path"
    }

    /** Server root shown to the user, e.g. `tanken.example.de` for `https://tanken.example.de/api/v1`. */
    public fun displayName(apiBaseUrl: String): String {
        val uri = try {
            URI(apiBaseUrl)
        } catch (_: URISyntaxException) {
            return apiBaseUrl
        }
        val port = if (uri.port == -1) "" else ":${uri.port}"
        val path = uri.rawPath.orEmpty().removeSuffix(API_PATH)
        return "${uri.host.orEmpty()}$port$path".trimEnd('/')
    }

    /**
     * Address of a page of the web app on the same server, e.g. `https://tanken.example.de/pages/impressum.php`
     * for `https://tanken.example.de/api/v1` and `pages/impressum.php`.
     *
     * The web app and the API share one origin; a self-hosted server serves its own legal pages.
     */
    public fun webPageUrl(page: String, apiBaseUrl: String): String {
        val root = apiBaseUrl.substringBefore('?').substringBefore('#').removeSuffix(API_PATH).trimEnd('/')
        return root + "/" + page.trimStart('/')
    }
}
