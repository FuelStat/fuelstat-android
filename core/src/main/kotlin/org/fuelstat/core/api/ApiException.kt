package org.fuelstat.core.api

import java.io.IOException

/** Error from a Fuelstat API call. */
public sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** The server answered with an error status. [code] is the API's error code, e.g. `invalid_credentials`. */
    public class Http(
        public val status: Int,
        public val code: String,
        public val body: ApiErrorBody?,
    ) : ApiException("HTTP $status: $code")

    /** The server could not be reached (offline, DNS, TLS, timeout). */
    public class Network(cause: IOException) : ApiException("Network error", cause)

    /** The server answered, but not with what the app expected (e.g. not a Fuelstat server). */
    public class InvalidResponse(cause: Throwable? = null) : ApiException("Invalid response", cause)
}

/** The session is no longer valid; the app has to sign in again. */
public val ApiException.isUnauthorized: Boolean
    get() = this is ApiException.Http && status == 401 &&
        code != "invalid_credentials" && code != "mfa_required" &&
        code != "invalid_mfa_code" && code != "invalid_mfa_token"
