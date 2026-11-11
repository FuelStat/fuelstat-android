package org.fuelstat.core.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModelsTest {
    @Test
    fun `vehicle accepts numbers as strings and unknown fuel types`() {
        val json = """
            {"id":"v1","name":"Golf","fuel_type":"plasma","brand":"VW","model":"Golf","year":"2019",
             "tank_capacity":"50.00","odometer_km":12345,"battery_capacity":null,
             "first_registration":"2019-03-01T00:00:00.000Z","extra":"ignored"}
        """.trimIndent()
        val vehicle = FuelstatJson.decodeFromString(Vehicle.serializer(), json)
        assertEquals(FuelType.OTHER, vehicle.fuelType)
        assertEquals(2019, vehicle.year)
        assertEquals(50.0, vehicle.tankCapacity)
        assertEquals(12345.0, vehicle.odometerKm)
        assertNull(vehicle.batteryCapacity)
        assertEquals("2019-03-01", vehicle.firstRegistrationDate)
        assertEquals("VW Golf", vehicle.subtitle)
    }

    @Test
    fun `known fuel type is decoded`() {
        val vehicle = FuelstatJson.decodeFromString(
            Vehicle.serializer(),
            """{"id":"v2","name":"Zoe","fuel_type":"electric","year":""}""",
        )
        assertEquals(FuelType.ELECTRIC, vehicle.fuelType)
        assertNull(vehicle.year)
        assertNull(vehicle.subtitle)
    }

    @Test
    fun `display name falls back to e-mail`() {
        assertEquals("a@b.de", User(id = "1", email = "a@b.de", username = " ").displayName)
        assertEquals("Mario", User(id = "1", email = "a@b.de", username = "Mario").displayName)
    }
}
