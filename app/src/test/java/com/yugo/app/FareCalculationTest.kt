package com.yugo.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FareCalculationTest {

    // Mirroring YUGO Ride Fare Model
    data class VehicleFareRule(
        val baseFare: Double,
        val perKmRate: Double,
        val perMinuteRate: Double,
        val minimumFare: Double
    )

    private val fareRules = mapOf(
        "MOTO" to VehicleFareRule(baseFare = 20.0, perKmRate = 8.0, perMinuteRate = 1.0, minimumFare = 30.0),
        "AUTO" to VehicleFareRule(baseFare = 30.0, perKmRate = 11.0, perMinuteRate = 1.5, minimumFare = 45.0),
        "GO" to VehicleFareRule(baseFare = 50.0, perKmRate = 14.0, perMinuteRate = 2.0, minimumFare = 80.0),
        "PREMIER" to VehicleFareRule(baseFare = 80.0, perKmRate = 19.0, perMinuteRate = 2.5, minimumFare = 120.0),
        "XL" to VehicleFareRule(baseFare = 110.0, perKmRate = 24.0, perMinuteRate = 3.0, minimumFare = 160.0)
    )

    private fun calculateFare(category: String, distanceKm: Double, durationMins: Double, surgeMultiplier: Double = 1.0): Double {
        val rule = fareRules[category] ?: throw IllegalArgumentException("Unknown vehicle category")
        val rawFare = (rule.baseFare + (distanceKm * rule.perKmRate) + (durationMins * rule.perMinuteRate)) * surgeMultiplier
        return Math.max(rule.minimumFare, Math.round(rawFare * 100.0) / 100.0)
    }

    @Test
    fun testMotoFareCalculation() {
        // 5 km, 15 mins -> 20 + (5 * 8) + (15 * 1) = 20 + 40 + 15 = 75
        val fare = calculateFare("MOTO", 5.0, 15.0)
        assertEquals(75.0, fare, 0.01)
    }

    @Test
    fun testAutoFareCalculation() {
        // 10 km, 25 mins -> 30 + (10 * 11) + (25 * 1.5) = 30 + 110 + 37.5 = 177.5
        val fare = calculateFare("AUTO", 10.0, 25.0)
        assertEquals(177.5, fare, 0.01)
    }

    @Test
    fun testPremierSurgePricing() {
        // 8 km, 20 mins -> 80 + (8 * 19) + (20 * 2.5) = 80 + 152 + 50 = 282
        // Surge 1.5x -> 282 * 1.5 = 423.0
        val fare = calculateFare("PREMIER", 8.0, 20.0, surgeMultiplier = 1.5)
        assertEquals(423.0, fare, 0.01)
    }

    @Test
    fun testMinimumFareEnforcement() {
        // Very short trip: 0.2 km, 1 min for XL
        // raw = 110 + (0.2 * 24) + (1 * 3) = 110 + 4.8 + 3 = 117.8
        // minimumFare = 160.0
        val fare = calculateFare("XL", 0.2, 1.0)
        assertEquals(160.0, fare, 0.01)
    }

    @Test
    fun testAllVehicleTiersOrdering() {
        val dist = 12.0
        val mins = 30.0
        val moto = calculateFare("MOTO", dist, mins)
        val auto = calculateFare("AUTO", dist, mins)
        val go = calculateFare("GO", dist, mins)
        val premier = calculateFare("PREMIER", dist, mins)
        val xl = calculateFare("XL", dist, mins)

        assertTrue(moto < auto)
        assertTrue(auto < go)
        assertTrue(go < premier)
        assertTrue(premier < xl)
    }
}
