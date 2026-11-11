package org.fuelstat.core.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/** JSON settings shared by the API client and the tests. */
public val FuelstatJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/** The signed-in account. Login sends only some fields; `/whoami` sends all of them. */
@Serializable
public data class User(
    val id: String,
    val email: String,
    val username: String? = null,
    @SerialName("mfa_enabled") val mfaEnabled: Boolean? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("pending_email") val pendingEmail: String? = null,
) {
    /** Name shown in the app: the user name, otherwise the e-mail address. */
    val displayName: String get() = username?.takeIf { it.isNotBlank() } ?: email
}

/** Response of `POST /auth/login`, `POST /auth/login/mfa` and `POST /auth/register`. */
@Serializable
public data class AuthResponse(val token: String, val user: User)

/** Response of `POST /auth/refresh`. */
@Serializable
public data class TokenResponse(val token: String)

/** Response of `GET /health` (no sign-in needed). */
@Serializable
public data class Health(
    val status: String,
    val version: String? = null,
    @SerialName("terms_version") val termsVersion: String? = null,
)

/** Error body of the API: `{ "error": "some_code" }`, plus details for some codes. */
@Serializable
public data class ApiErrorBody(
    val error: String,
    val detail: String? = null,
    /** Only for `mfa_required`: short-lived token for `POST /auth/login/mfa`. */
    val mfaToken: String? = null,
    /** Only for `plan_required`: which plan limit was hit, its value and the current plan. */
    val feature: String? = null,
    @Serializable(with = LenientIntSerializer::class) val limit: Int? = null,
    val plan: String? = null,
)

/** Fuel type as delivered by the API. Unknown values decode to [OTHER]. */
@Serializable(with = FuelTypeSerializer::class)
public enum class FuelType(public val apiValue: String) {
    DIESEL("diesel"),
    E5("e5"),
    E10("e10"),
    LPG("lpg"),
    CNG("cng"),
    ELECTRIC("electric"),
    HYBRID("hybrid"),
    PHEV("phev"),
    HYDROGEN("hydrogen"),
    OTHER("other"),
    ;

    public companion object {
        public fun fromApi(value: String): FuelType = entries.firstOrNull { it.apiValue == value } ?: OTHER
    }
}

/** Vehicle from `GET /vehicles`. */
@Serializable
public data class Vehicle(
    val id: String,
    val name: String,
    @SerialName("fuel_type") val fuelType: FuelType = FuelType.OTHER,
    val brand: String? = null,
    val model: String? = null,
    @Serializable(with = LenientIntSerializer::class) val year: Int? = null,
    /** Signed, relative address of the photo (`/api/v1/files/…?exp=…&sig=…`), valid until the end of the next day. */
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("license_plate") val licensePlate: String? = null,
    /** Calendar date; the API may append a time, use [firstRegistrationDate]. */
    @SerialName("first_registration") val firstRegistration: String? = null,
    @Serializable(with = LenientDoubleSerializer::class)
    @SerialName("odometer_km")
    val odometerKm: Double? = null,
    @Serializable(with = LenientDoubleSerializer::class)
    @SerialName("tank_capacity")
    val tankCapacity: Double? = null,
    @Serializable(with = LenientDoubleSerializer::class)
    @SerialName("battery_capacity")
    val batteryCapacity: Double? = null,
) {
    /** First registration as `YYYY-MM-DD`. */
    val firstRegistrationDate: String? get() = firstRegistration?.take(10)?.takeIf { it.isNotBlank() }

    /** Brand and model, e.g. "VW Golf"; null if neither is set. */
    val subtitle: String?
        get() = listOfNotNull(brand, model).map { it.trim() }.filter { it.isNotEmpty() }
            .joinToString(" ").takeIf { it.isNotEmpty() }
}

internal object FuelTypeSerializer : KSerializer<FuelType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FuelType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): FuelType = FuelType.fromApi(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: FuelType): Unit = encoder.encodeString(value.apiValue)
}

/**
 * PostgreSQL `numeric` columns reach the client as strings (`"52.50"`), other numbers as JSON numbers.
 * Accepts both; empty or unparsable values become null.
 */
internal object LenientDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientDouble", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double? {
        val element = (decoder as? JsonDecoder)?.decodeJsonElement() ?: return decoder.decodeDouble()
        if (element !is JsonPrimitive || element is JsonNull) return null
        return element.doubleOrNull ?: element.content.trim().replace(',', '.').toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeNull() else encoder.encodeDouble(value)
    }
}

/** Like [LenientDoubleSerializer], for whole numbers (`"2019"`, `2019`, `2019.0`). */
internal object LenientIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? = LenientDoubleSerializer.deserialize(decoder)?.toInt()

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }
}
